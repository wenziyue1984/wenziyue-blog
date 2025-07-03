package com.wenziyue.blog.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.security.AuthHelper;
import com.wenziyue.blog.biz.service.BizArticleService;
import com.wenziyue.blog.biz.utils.IdUtils;
import com.wenziyue.blog.common.exception.BlogResultCode;
import com.wenziyue.blog.common.utils.BlogUtils;
import com.wenziyue.blog.dal.dto.ArticleDTO;
import com.wenziyue.blog.dal.dto.SlugDTO;
import com.wenziyue.blog.dal.dto.SummaryDTO;
import com.wenziyue.blog.dal.entity.ArticleEntity;
import com.wenziyue.blog.dal.entity.ArticleTagEntity;
import com.wenziyue.blog.dal.entity.TagEntity;
import com.wenziyue.blog.dal.service.ArticleService;
import com.wenziyue.blog.dal.service.ArticleTagService;
import com.wenziyue.blog.dal.service.TagService;
import com.wenziyue.framework.common.CommonCode;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.uid.core.IdGen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wenziyue.blog.common.constants.RedisConstant.ARTICLE_VERSION_KEY;
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
    public String save(ArticleDTO dto) {
        if (dto == null) {
            throw new ApiException(CommonCode.ILLEGAL_PARAMS);
        }
        val title = BlogUtils.safeTrimEmptyIsNull(dto.getTitle());
        val summary = BlogUtils.safeTrimEmptyIsNull(dto.getSummary());
        val content = BlogUtils.safeTrimEmptyIsNull(dto.getContent());
        if (title == null) {
            throw new ApiException(BlogResultCode.ARTICLE_TITLE_EMPTY);
        }
        if (!articleService.list(Wrappers.<ArticleEntity>lambdaQuery()
                .select(ArticleEntity::getTitle)
                .eq(ArticleEntity::getTitle, title)
                .eq(ArticleEntity::getUserId, authHelper.getCurrentUser().getId())).isEmpty()) {
            throw new ApiException(BlogResultCode.ARTICLE_TITLE_REPEAT);
        }
        if (content == null) {
            throw new ApiException(BlogResultCode.ARTICLE_CONTENT_EMPTY);
        }

        try {
            // 保存文章
            Long userId = authHelper.getCurrentUser().getId();
            ArticleEntity articleEntity = ArticleEntity.builder()
                    .id(IdUtils.getID(idGen))
                    .title(title)
                    .content(content)
                    .summary(summary)
                    .coverUrl(dto.getCoverUrl())
                    .userId(userId)
                    .keywords(dto.getKeywords())
                    .isTop(dto.getIsTop()).build();
            articleService.save(articleEntity);

            // 保存tag对应关系
            if (dto.getTagList() != null && !dto.getTagList().isEmpty()) {
                val tagEntityList = tagService.list(Wrappers.<TagEntity>lambdaQuery().in(TagEntity::getName, dto.getTagList()));
                if (!tagEntityList.isEmpty()) {
                    List<ArticleTagEntity> atList = new ArrayList<>();
                    for (TagEntity tagEntity : tagEntityList) {
                        atList.add(ArticleTagEntity.builder()
                                .articleId(articleEntity.getId())
                                .tagId(tagEntity.getId())
                                .build());
                    }
                    articleTagService.saveBatch(atList);
                }
            }

            // 异步添加摘要和slug
            if (summary == null) {
                executorService.submit(() -> sendSummaryMq(dto.getTitle(), dto.getContent(), userId, articleEntity.getId(), 0));
            }

            return articleEntity.getId().toString();
        } catch (Exception e) {
            log.error("保存文章失败:{}", dto, e);
            throw new ApiException(BlogResultCode.ARTICLE_SAVE_ERROR);
        }
    }

    @Override
    public void sendSummaryMq(String title, String content, Long userId, Long articleId, Integer version) {
        try {
            log.debug("发送摘要mq，文章id:{}", articleId);
            val slugList = articleService.list(Wrappers.<ArticleEntity>lambdaQuery()
                            .select(ArticleEntity::getSlug)
                            .eq(ArticleEntity::getUserId, userId).isNotNull(ArticleEntity::getSlug))
                    .stream().map(ArticleEntity::getSlug).filter(Objects::nonNull).collect(Collectors.toList());
            Message<SummaryDTO> message = MessageBuilder.withPayload(new SummaryDTO(title, content, slugList, articleId, version)).build();
            val sendResult = rocketMQTemplate.syncSend(SummaryTopic, message);
            if (sendResult == null || !sendResult.getSendStatus().equals(SendStatus.SEND_OK)) {
                log.error("加入summary的mq失败，文章id:{}, sendResult:{}", articleId, sendResult);
                return;
            }
            // 记录当前的文章版本，以防生成summary和slug的时候文章已被修改
            redisUtils.set(ARTICLE_VERSION_KEY + articleId, version, 12, TimeUnit.HOURS);
        } catch (Exception e) {
            log.info("发送摘要mq失败，文章id:{}", articleId, e);
        }
    }


}
