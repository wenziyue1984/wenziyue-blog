package com.wenziyue.blog.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.security.AuthHelper;
import com.wenziyue.blog.biz.service.BizArticleService;
import com.wenziyue.blog.biz.utils.IdUtils;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.common.enums.*;
import com.wenziyue.blog.common.exception.BlogResultCode;
import com.wenziyue.blog.common.utils.BlogUtils;
import com.wenziyue.blog.dal.dto.*;
import com.wenziyue.blog.dal.entity.ArticleEntity;
import com.wenziyue.blog.dal.entity.ArticleOperationLogEntity;
import com.wenziyue.blog.dal.entity.ArticleTagEntity;
import com.wenziyue.blog.dal.entity.TagEntity;
import com.wenziyue.blog.dal.service.*;
import com.wenziyue.framework.common.CommonCode;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.mybatisplus.page.PageResult;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.uid.core.IdGen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wenziyue.blog.common.constants.RedisConstant.ARTICLE_UPDATE_TIME_KEY;
import static com.wenziyue.blog.common.constants.RedisConstant.SLUG_LISTEN_KEY;
import static com.wenziyue.blog.common.constants.RocketTopic.SlugTopic;
import static com.wenziyue.blog.common.constants.RocketTopic.SummaryTopic;

/**
 * @author wenziyue
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BizArticleServiceImpl implements BizArticleService {

    private final ArticleService articleService;
    private final TagService tagService;
    private final ArticleTagService articleTagService;
    private final RocketMQTemplate rocketMQTemplate;
    private final AuthHelper authHelper;
    private final RedisUtils redisUtils;
    private final IdGen idGen;
    private final ExecutorService executorService;
    private final ArticleOperationLogService articleOperationLogService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Boolean> likeArticleScript;
    private final RedisScript<Boolean> cancelLikeArticleScript;

    @Override
    public String generateSlug(SlugDTO dto) {
        // 加入mq队列
        Long userId = authHelper.getCurrentUser().getId();
        String listenKey = SLUG_LISTEN_KEY + userId + ":" + UUID.randomUUID();
        dto.setListenKey(listenKey);
        val articleEntityList = articleService.list(Wrappers.<ArticleEntity>lambdaQuery().select(ArticleEntity::getSlug)
                .eq(ArticleEntity::getUserId, userId));
        if (!articleEntityList.isEmpty()) {
            dto.setUsedSlugs(articleEntityList.stream().map(ArticleEntity::getSlug).collect(Collectors.toList()));
        }
        Message<SlugDTO> message = MessageBuilder.withPayload(dto).build();
        val sendResult = rocketMQTemplate.syncSend(SlugTopic, message);
        if (sendResult == null || !sendResult.getSendStatus().equals(SendStatus.SEND_OK)) {
            log.error("slug加入mq失败:{}", sendResult);
            throw new ApiException(BlogResultCode.SLUG_GENERATE_ERROR);
        }
        return listenKey;
    }

    @Override
    public String getSlug(String listenKey) {
        Object slug = redisUtils.get(listenKey);
        if (slug != null) {
            // 本来用完后应该删除key的，但是考虑到删除之后就再也无法查询到这次的slug了，可能因为某些原因还要再次查询，所以这里就不删除了，而slugworker中会设置过期时间，所以这里就不进行处理了
//            redisUtils.delete(listenKey);
            return slug.toString();
        }
        return null;
    }

    @Override
    public String saveOrUpdate(ArticleDTO dto) {
        if (dto == null) {
            throw new ApiException(CommonCode.ILLEGAL_PARAMS);
        }
        val title = BlogUtils.safeTrimEmptyIsNull(dto.getTitle());
        val content = BlogUtils.safeTrimEmptyIsNull(dto.getContent());
        if (title == null) {
            throw new ApiException(BlogResultCode.ARTICLE_TITLE_EMPTY);
        }
        if (articleService.count(Wrappers.<ArticleEntity>lambdaQuery()
                .eq(ArticleEntity::getTitle, title)
                .ne(dto.getId() != null, ArticleEntity::getId, dto.getId())
                .eq(ArticleEntity::getUserId, authHelper.getCurrentUser().getId())) > 0) {
            throw new ApiException(BlogResultCode.ARTICLE_TITLE_REPEAT);
        }
        if (content == null) {
            throw new ApiException(BlogResultCode.ARTICLE_CONTENT_EMPTY);
        }

        try {
            // 保存文章
            Long userId = authHelper.getCurrentUser().getId();
            ArticleEntity articleEntity;
            if (dto.getId() == null) {
                // 新建
                articleEntity = ArticleEntity.builder()
                        .id(IdUtils.getID(idGen))
                        .title(title)
                        .content(content)
                        .coverUrl(dto.getCoverUrl())
                        .userId(userId)
                        .build();
            } else {
                // 修改
                articleEntity = articleService.getById(dto.getId());
                if (articleEntity == null) {
                    throw new ApiException(BlogResultCode.ARTICLE_NOT_EXIST);
                }
                if (!articleEntity.getUserId().equals(authHelper.getCurrentUser().getId())) {
                    throw new ApiException(BlogResultCode.USER_NO_PERMISSION);
                }
                articleEntity.setTitle(title);
                articleEntity.setContent(content);
                articleEntity.setCoverUrl(dto.getCoverUrl());
            }
            //下面提交mq时中会使用updateTime，但是这里saveOrUpdate后事务还未提交，updateTime值还未更新，所以这里手动set一下
            articleEntity.setUpdateTime(LocalDateTime.now());
            articleService.saveOrUpdate(articleEntity);

            // 保存tag对应关系
            List<Long> addTagIdList = new ArrayList<>();
            List<Long> deleteTagIdList = new ArrayList<>();
            if (dto.getTagList() != null && !dto.getTagList().isEmpty()) {
                //去重
                Set<Long> tagIdSet = dto.getTagList().stream().map(TagDTO::getId).filter(Objects::nonNull).collect(Collectors.toSet());
                val tagEntityList = tagService.list(Wrappers.<TagEntity>lambdaQuery()
                        .select(TagEntity::getId)
                        .eq(TagEntity::getStatus, TagStatusEnum.ENABLED)
                        .in(TagEntity::getId, tagIdSet));

                if (dto.getId() == null) {
                    // 新建文章时
                    if (!tagEntityList.isEmpty()) {
                        for (TagEntity tagEntity : tagEntityList) {
                            addTagIdList.add(tagEntity.getId());
                        }
                    }
                } else {
                    // 修改文章时
                    val oldArticleTagList = articleTagService.list(Wrappers.<ArticleTagEntity>lambdaQuery()
                            .eq(ArticleTagEntity::getArticleId, articleEntity.getId()));
                    // 需要删除的tagId
                    deleteTagIdList = oldArticleTagList.stream().map(ArticleTagEntity::getTagId).filter(tagId -> !tagIdSet.contains(tagId)).collect(Collectors.toList());
                    // 需要新加的tagId
                    val oldATIdSet = oldArticleTagList.stream().map(ArticleTagEntity::getTagId).collect(Collectors.toSet());
                    addTagIdList = tagEntityList.stream().map(TagEntity::getId).filter(tagId -> !oldATIdSet.contains(tagId)).collect(Collectors.toList());
                }
            } else if (dto.getId() != null) {
                // 需要删除所有的标签
                val oldArticleTagList = articleTagService.list(Wrappers.<ArticleTagEntity>lambdaQuery()
                        .eq(ArticleTagEntity::getArticleId, articleEntity.getId()));
                if (!oldArticleTagList.isEmpty()) {
                    deleteTagIdList = oldArticleTagList.stream().map(ArticleTagEntity::getTagId).collect(Collectors.toList());
                }
            }
            // 插入
            if (!addTagIdList.isEmpty()) {
                val addList = new ArrayList<ArticleTagEntity>();
                for (Long tagId : addTagIdList) {
                    addList.add(ArticleTagEntity.builder()
                            .articleId(articleEntity.getId())
                            .tagId(tagId)
                            .build());
                }
                articleTagService.saveBatch(addList);
            }
            // 删除
            if (!deleteTagIdList.isEmpty()) {
                articleTagService.remove(Wrappers.<ArticleTagEntity>lambdaQuery()
                        .eq(ArticleTagEntity::getArticleId, articleEntity.getId())
                        .in(ArticleTagEntity::getTagId, deleteTagIdList));
            }

            // 保存操作日志
            articleOperationLogService.save(ArticleOperationLogEntity.builder()
                    .id(IdUtils.getID(idGen))
                    .articleId(articleEntity.getId())
                    .operatorId(userId)
                    .operatorName(authHelper.getCurrentUser().getName())
                    .operationType(dto.getId() == null ? ArticleOperationTypeEnum.ADD :ArticleOperationTypeEnum.UPDATE)
                    .data(dto.toString())
                    .build());

            // 异步添加摘要和slug
            executorService.submit(() -> sendSummaryMq(dto.getTitle(), dto.getContent(), userId, articleEntity.getId(), articleEntity.getUpdateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

            return articleEntity.getId().toString();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存文章失败:{}", dto, e);
            throw new ApiException(BlogResultCode.ARTICLE_SAVE_OR_UPDATE_ERROR);
        }
    }

    @Override
    public void sendSummaryMq(String title, String content, Long userId, Long articleId, String updateTime) {
        try {
            log.info("发送摘要mq，文章id:{}", articleId);
            val slugList = articleService.list(Wrappers.<ArticleEntity>lambdaQuery()
                            .select(ArticleEntity::getSlug)
                            .eq(ArticleEntity::getUserId, userId).isNotNull(ArticleEntity::getSlug))
                    .stream().map(ArticleEntity::getSlug).filter(Objects::nonNull).collect(Collectors.toList());
            Message<SummaryDTO> message = MessageBuilder.withPayload(new SummaryDTO(title, content, slugList, articleId, updateTime)).build();
            val sendResult = rocketMQTemplate.syncSend(SummaryTopic, message);
            if (sendResult == null || !sendResult.getSendStatus().equals(SendStatus.SEND_OK)) {
                log.error("加入summary的mq失败，文章id:{}, sendResult:{}", articleId, sendResult);
                return;
            }
            // 记录当前的文章版本，以防生成summary和slug的时候文章已被修改
            redisUtils.set(ARTICLE_UPDATE_TIME_KEY + articleId, updateTime, 12, TimeUnit.HOURS);
        } catch (Exception e) {
            log.info("发送摘要mq失败，文章id:{}", articleId, e);
        }
    }


    @Override
    public TagDTO getTag(TagDTO dto) {
        if (dto == null) {
            throw new ApiException(CommonCode.ILLEGAL_PARAMS);
        }
        val one = tagService.getOne(Wrappers.<TagEntity>lambdaQuery()
                .select(TagEntity::getId, TagEntity::getName)
                .ge(TagEntity::getName, dto.getName()));
        if (one != null) {
            return TagDTO.builder().id(one.getId()).name(one.getName()).build();
        }
        TagEntity tagEntity = TagEntity.builder().id(IdUtils.getID(idGen)).name(dto.getName()).build();
        tagService.save(tagEntity);
        return TagDTO.builder().id(tagEntity.getId()).name(tagEntity.getName()).build();
    }

    @Override
    public void changeTagStatus(TagDTO dto) {
        if (dto == null || dto.getId() == null || dto.getStatus() == null || dto.getStatus() > 1 || dto.getStatus() < 0) {
            throw new ApiException(CommonCode.ILLEGAL_PARAMS);
        }
        val byId = tagService.getById(dto.getId());
        if (byId == null) {
            throw new ApiException(BlogResultCode.ARTICLE_TAT_EMPTY);
        }
        if (Objects.equals(byId.getStatus().getCode(), dto.getStatus())) {
            return;
        }
        tagService.update(Wrappers.<TagEntity>lambdaUpdate()
                .set(TagEntity::getStatus, dto.getStatus())
                .eq(TagEntity::getId, dto.getId()));
    }

    @Override
    public ArticleDTO getArticleDetail(Long id) {
        val articleEntity = articleService.getById(id);
        if (articleEntity == null) {
            throw new ApiException(BlogResultCode.ARTICLE_NOT_EXIST);
        }
        // 只有管理员和作者可以查看隐藏的文章
        if (articleEntity.getStatus().equals(ArticleStatusEnum.HIDDEN)) {
            if (!authHelper.getCurrentUser().getRole().equals(UserRoleEnum.ADMIN) && !articleEntity.getUserId().equals(authHelper.getCurrentUser().getId())) {
                throw new ApiException(BlogResultCode.ARTICLE_NOT_EXIST);
            }
        }
        // 查询标签
        List<TagDTO> tagList = articleTagService.getEnabledTags(id);
        return ArticleDTO.builder()
                .id(articleEntity.getId())
                .title(articleEntity.getTitle())
                .content(articleEntity.getContent())
                .summary(articleEntity.getSummary())
                .coverUrl(articleEntity.getCoverUrl())
                .tagList(tagList)
                .viewCount(articleEntity.getViewCount())
                .likeCount(articleEntity.getLikeCount())
                .slug(articleEntity.getSlug())
                .isTop(articleEntity.getIsTop())
                .sort(articleEntity.getSort())
                .status(articleEntity.getStatus())
                .build();
    }

    @Override
    public PageResult<ArticleDTO> pageArticle(ArticlePageDTO dto) {
        val currentUser = authHelper.getCurrentUser();
        if (dto.getUserId() == null && currentUser.getRole().equals(UserRoleEnum.USER)) {
            dto.setUserId(currentUser.getId());
        }
        if (currentUser.getRole().equals(UserRoleEnum.USER) && currentUser.getId().equals(dto.getUserId())) {
            dto.setFilterHidden(true);
        }

        return articleService.page(dto);
    }

    @Override
    public PageResult<TagEntity> pageArticleTag(TagPageDTO dto) {
        return tagService.page(dto, Wrappers.<TagEntity>lambdaQuery()
                .eq(dto.getId() != null, TagEntity::getId, dto.getId())
                .eq(dto.getName() != null && !dto.getName().isEmpty(), TagEntity::getName, dto.getName())
                .eq(dto.getStatus() != null, TagEntity::getStatus, dto.getStatus()));
    }

    @Override
    public void hideArticle(Long id) {
        // 管理员以及用户自己可以隐藏文章
        val currentUser = authHelper.getCurrentUser();
        if (!currentUser.getRole().equals(UserRoleEnum.ADMIN) && !currentUser.getId().equals(articleService.getById(id).getUserId())) {
            throw new ApiException(BlogResultCode.USER_NO_PERMISSION);
        }
        val articleEntity = articleService.getById(id);
        if (articleEntity == null) {
            throw new ApiException(BlogResultCode.ARTICLE_NOT_EXIST);
        }
        if (articleEntity.getStatus().equals(ArticleStatusEnum.HIDDEN)) {
            return;
        }
        articleEntity.setStatus(ArticleStatusEnum.HIDDEN);
        articleService.updateById(articleEntity);
    }

    @Override
    public void deleteArticle(Long id) {
        // 管理员以及用户自己可以操作
        val currentUser = authHelper.getCurrentUser();
        if (!currentUser.getRole().equals(UserRoleEnum.ADMIN) && !currentUser.getId().equals(articleService.getById(id).getUserId())) {
            throw new ApiException(BlogResultCode.USER_NO_PERMISSION);
        }
        val articleEntity = articleService.getById(id);
        if (articleEntity == null) {
            throw new ApiException(BlogResultCode.ARTICLE_NOT_EXIST);
        }
        articleService.removeById(id);
        // 删除redis中的点赞数据
        redisUtils.delete(RedisConstant.ARTICLE_LIKE_COUNT_KEY + id);
        redisUtils.delete(RedisConstant.ARTICLE_LIKE_USERS_KEY + id);
    }

    @Override
    public void setTopArticle(Long id) {
        // 管理员以及用户自己可以操作
        val currentUser = authHelper.getCurrentUser();
        if (!currentUser.getRole().equals(UserRoleEnum.ADMIN) && !currentUser.getId().equals(articleService.getById(id).getUserId())) {
            throw new ApiException(BlogResultCode.USER_NO_PERMISSION);
        }
        val articleEntity = articleService.getById(id);
        if (articleEntity == null) {
            throw new ApiException(BlogResultCode.ARTICLE_NOT_EXIST);
        }
        if (articleEntity.getIsTop()) {
            return;
        }
        if (articleEntity.getStatus().equals(ArticleStatusEnum.HIDDEN)) {
            throw new ApiException(BlogResultCode.HIDDEN_CANNOT_SET_TOP);
        }

        val topArticleList = articleService.list(Wrappers.<ArticleEntity>lambdaQuery()
                .select(ArticleEntity::getId, ArticleEntity::getSort)
                .eq(ArticleEntity::getUserId, articleEntity.getUserId())
                .eq(ArticleEntity::getIsTop, true)
                .eq(ArticleEntity::getStatus, ArticleStatusEnum.NORMAL));
        if (topArticleList.size() > 2) {
            throw new ApiException(BlogResultCode.USER_TOP_ARTICLE_LIMIT);
        }
        if (!topArticleList.isEmpty()) {
            int maxSort = topArticleList.stream()
                    .map(ArticleEntity::getSort)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0); // 如果列表为空或者都是 null，则默认返回 0
            articleEntity.setSort(maxSort + 1);
        }
        articleEntity.setIsTop(true);
        articleService.updateById(articleEntity);
    }

    @Override
    public void cancelTopArticle(Long id) {
        // 管理员以及用户自己可以操作
        val currentUser = authHelper.getCurrentUser();
        if (!currentUser.getRole().equals(UserRoleEnum.ADMIN) && !currentUser.getId().equals(articleService.getById(id).getUserId())) {
            throw new ApiException(BlogResultCode.USER_NO_PERMISSION);
        }
        val articleEntity = articleService.getById(id);
        if (articleEntity == null) {
            throw new ApiException(BlogResultCode.ARTICLE_NOT_EXIST);
        }
        if (articleEntity.getIsTop()) {
            return;
        }
        articleEntity.setIsTop(false);
        articleEntity.setSort(0);
        articleService.updateById(articleEntity);
    }

    @Override
    public void likeArticle(Long articleId) {
        val articleEntity = articleService.getById(articleId);
        if (articleEntity == null) {
            throw new ApiException(BlogResultCode.ARTICLE_NOT_EXIST);
        }
        val userId = authHelper.getCurrentUser().getId();
        // 不能点赞自己的文章
        if (articleEntity.getUserId().equals(userId)) {
            throw new ApiException(BlogResultCode.USER_CANNOT_LIKE_SELF_ARTICLE);
        }
        // 是否已经点赞过
        if (redisUtils.zScore(RedisConstant.USER_LIKE_ARTICLES_KEY + userId, articleId) != null) {
            log.warn("用户{}已经点赞过文章:{}", userId, articleId);
            return;
        }
//        // 添加到用户点赞队列中
//        redisUtils.zAdd(RedisConstant.USER_LIKE_ARTICLES_KEY + userId, articleId, System.currentTimeMillis());
//        // 文章点赞总数+1
//        redisUtils.increment(RedisConstant.ARTICLE_LIKE_COUNT_KEY + articleId, 1);
//        // 添加到文章点赞队列中（只保留前50名点赞用户）
//        if (redisUtils.zSize(RedisConstant.ARTICLE_LIKE_USERS_KEY + articleId) < 50) {
//            redisUtils.zAdd(RedisConstant.ARTICLE_LIKE_USERS_KEY + articleId, userId, System.currentTimeMillis());
//        }
//        // 记录点赞行为
//        Map<String, String> map = new HashMap<>();
//        map.put("articleId", articleId.toString());
//        map.put("userId", userId.toString());
//        map.put("time", String.valueOf(System.currentTimeMillis()));
//        map.put("type", String.valueOf(ArticleLikeTypeEnum.LIKE.getCode()));
//        redisUtils.xAdd(RedisConstant.ARTICLE_LIKE_STREAM_KEY, map);

        // lua脚本事务执行（添加到用户点赞队列中，文章点赞总数+1，前50个点赞者添加到文章点赞队列中，记录点赞行为）
        List<String> keys = Arrays.asList(
                RedisConstant.USER_LIKE_ARTICLES_KEY + userId,
                RedisConstant.ARTICLE_LIKE_COUNT_KEY + articleId,
                RedisConstant.ARTICLE_LIKE_USERS_KEY + articleId,
                RedisConstant.ARTICLE_LIKE_STREAM_KEY
        );
        List<Object> args = Arrays.asList(
                userId,
                articleId,
                System.currentTimeMillis(),
                50,
                ArticleLikeTypeEnum.LIKE.getCode()
        );
        val result = redisTemplate.execute(likeArticleScript, keys, args.toArray());
        if (result == null || !result) {
            throw new ApiException(BlogResultCode.USER_LIKE_ERROR);
        }
    }

    @Override
    public void cancelLikeArticle(Long articleId) {
        val articleEntity = articleService.getById(articleId);
        if (articleEntity == null) {
            throw new ApiException(BlogResultCode.ARTICLE_NOT_EXIST);
        }
        val userId = authHelper.getCurrentUser().getId();
        // 查询是否有点赞
        if (redisUtils.zScore(RedisConstant.USER_LIKE_ARTICLES_KEY + userId, articleId) == null) {
            return;
        }
//        // 删除用户点赞
//        redisUtils.zRemove(RedisConstant.USER_LIKE_ARTICLES_KEY + userId, articleId);
//        // 如果文章点赞zaet中有该用户删除之
//        if (redisUtils.zScore(RedisConstant.ARTICLE_LIKE_USERS_KEY + articleId, userId) != null) {
//            redisUtils.zRemove(RedisConstant.ARTICLE_LIKE_USERS_KEY + articleId, userId);
//        }
//        // 文章点赞数-1
//        redisUtils.increment(RedisConstant.ARTICLE_LIKE_COUNT_KEY + articleId, -1);
//        // 记录取消点赞行为
//        Map<String, String> map = new HashMap<>();
//        map.put("articleId", articleId.toString());
//        map.put("userId", userId.toString());
//        map.put("time", String.valueOf(System.currentTimeMillis()));
//        map.put("type", String.valueOf(ArticleLikeTypeEnum.CANCEL_LIKE.getCode()));
//        redisUtils.xAdd(RedisConstant.ARTICLE_LIKE_STREAM_KEY, map);

        // lua脚本事务执行（删除用户点赞，如果文章点赞zaet中有该用户删除之，文章点赞数-1，记录取消点赞行为）
        List<String> keys = Arrays.asList(
                RedisConstant.USER_LIKE_ARTICLES_KEY + userId,
                RedisConstant.ARTICLE_LIKE_COUNT_KEY + articleId,
                RedisConstant.ARTICLE_LIKE_USERS_KEY + articleId,
                RedisConstant.ARTICLE_LIKE_STREAM_KEY
        );
        List<Object> args = Arrays.asList(
                userId,
                articleId,
                System.currentTimeMillis(),
                ArticleLikeTypeEnum.CANCEL_LIKE.getCode()
        );
        Boolean result = redisTemplate.execute(cancelLikeArticleScript, keys, args.toArray());
        if (result == null || !result) {
            throw new ApiException(BlogResultCode.USER_CANCEL_LIKE_ERROR);
        }
    }

    @PostConstruct
    public void init() {
        if (redisUtils.hasKey(RedisConstant.ARTICLE_LIKE_STREAM_KEY)) {
            return;
        }
        redisUtils.xGroupCreate(RedisConstant.ARTICLE_LIKE_STREAM_KEY, RedisConstant.ARTICLE_LIKE_STREAM_GROUP_NAME);
    }


}
