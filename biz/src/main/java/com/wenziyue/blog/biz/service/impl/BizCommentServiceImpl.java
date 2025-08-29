package com.wenziyue.blog.biz.service.impl;

import com.wenziyue.blog.biz.security.AuthHelper;
import com.wenziyue.blog.biz.service.BizCommentService;
import com.wenziyue.blog.biz.utils.IdUtils;
import com.wenziyue.blog.common.annotation.WzyRateLimiter;
import com.wenziyue.blog.common.enums.CommentDepthEnum;
import com.wenziyue.blog.common.enums.LikeTypeEnum;
import com.wenziyue.blog.common.exception.BlogResultCode;
import com.wenziyue.blog.common.utils.BlogUtils;
import com.wenziyue.blog.dal.dto.CommentDTO;
import com.wenziyue.blog.dal.dto.CommentLikeMqDTO;
import com.wenziyue.blog.dal.dto.CommentPageDTO;
import com.wenziyue.blog.dal.entity.CommentEntity;
import com.wenziyue.blog.dal.service.ArticleService;
import com.wenziyue.blog.dal.service.CommentService;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.mybatisplus.page.PageResult;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.uid.core.IdGen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wenziyue.blog.common.constants.RedisConstant.*;
import static com.wenziyue.blog.common.constants.RocketMqTopic.CommentLikeTopic;

/**
 * @author wenziyue
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizCommentServiceImpl implements BizCommentService {

    private final CommentService commentService;
    private final ArticleService articleService;
    private final AuthHelper authHelper;
    private final RedisUtils redisUtils;
    private final IdGen idGen;
    private final RocketMQTemplate rocketMQTemplate;

    @Override
    @WzyRateLimiter(key = "'comment:postComment:' + #userId", window = 60000, maxCount = 10, message = "评论太频繁")
    public Long postComment(CommentDTO dto, Long userId) {
        val content = BlogUtils.safeTrimEmptyIsNull(dto.getContent());
        if (content == null) {
            throw new ApiException(BlogResultCode.COMMENT_CONTENT_EMPTY);
        }
        // 防止刷评论
        check(content, dto.getArticleId(), userId);
        val articleEntity = articleService.getById(dto.getArticleId());
        if (articleEntity == null) {
            throw new ApiException(BlogResultCode.ARTICLE_NOT_EXIST);
        }
        Long replyUserId = articleEntity.getUserId();
        if (dto.getParentId() != null) {
            val parentCommentEntity = commentService.getById(dto.getParentId());
            if (parentCommentEntity == null) {
                throw new ApiException(BlogResultCode.COMMENT_NOT_EXIST);
            }
            replyUserId = parentCommentEntity.getUserId();
        }
        val id = IdUtils.getID(idGen);
        commentService.save(CommentEntity.builder()
                .id(id)
                .content(content)
                .articleId(dto.getArticleId())
                .authorId(articleEntity.getUserId())
                .parentId(dto.getParentId())
                .userId(userId)
                .replyUserId(replyUserId)
                .depth(dto.getParentId()==null ? CommentDepthEnum.ONE_LEVEL : CommentDepthEnum.TWO_LEVEL)
                .build());
        redisUtils.zAdd(COMMENT_CHECK_KEY + userId, BlogUtils.fp(dto.getArticleId() + ":" + content), System.currentTimeMillis(), 1L, TimeUnit.MINUTES);
        return id;
    }

    /**
     * 判断有没有刷评论，
     * 1.每个用户每分钟只能发十条评论（现在把限流放在了统一注解里，这里不再需要检查）
     * 2.并且每篇文章下同样内容的评论每分钟只能发一次
     * 思路：
     * 用zset，过期时间1min，zset的score就是评论时间，这样每次评论的时候，先去zset中查询，
     * 首先先清理一下zset中score超过1min的评论，然后看剩余的评论数量是否超过10条，如果超过则返回错误，
     * 然后再判断评论有没有重复，如果有则返回错误，如果没有则插入zset中，并且重置zset过期时间，返回成功
     */
    @SuppressWarnings("ConstantConditions")
    private void check(String content, Long articleId, Long userId) {
        String checkKey = COMMENT_CHECK_KEY + userId;
        if (!redisUtils.hasKey(checkKey)) {
            return;
        }
        // 清理过期评论
        redisUtils.zRemoveRangeByScore(checkKey, 0, System.currentTimeMillis() - 60000);
        val objects = redisUtils.zRange(checkKey, 0, -1);
        // 现在把限流放在了统一注解里，这里不再需要检查
//        // 检查一分钟内的评论次数是否超过10
//        if (objects.size() >= 10) {
//            throw new ApiException(BlogResultCode.COMMENT_OVER_TEN_TIMES);
//        }
        // 检查评论是否重复
        val fp = BlogUtils.fp(articleId + ":" + content);
        val commentFpSet = objects.stream().map(Object::toString).collect(Collectors.toSet());
        if (commentFpSet.contains(fp)) {
            throw new ApiException(BlogResultCode.COMMENT_CONTENT_REPEAT);
        }
    }

    @Override
    @WzyRateLimiter(key = "'comment:likeComment:' + #userId", window = 60000, maxCount = 30, message = "点赞太频繁")
    public void likeComment(Long commentId, Long userId) {
        likeOrCancelLike(commentId, userId, LikeTypeEnum.LIKE.getCode());
    }

    /**
     * 取消点赞
     * 取消点赞和点赞一样都要走mq，不然用户点赞后立马取消，此时点赞还在mq中排队，没有修改redis计数，也没有落库，你直接取消点赞会发现并没有点赞过，
     * 所以必须让点赞和取消点赞都走mq，并且用rocketMQTemplate.syncSendOrderly()对同一评论进行顺序消费
     */
    @Override
    public void cancelLikeComment(Long commentId) {
        likeOrCancelLike(commentId, authHelper.getCurrentUser().getId(), LikeTypeEnum.CANCEL_LIKE.getCode());
    }

    private void likeOrCancelLike(Long commentId, Long userId, int type) {
        val dto = CommentLikeMqDTO.builder().commentId(commentId).userId(userId).type(type).build();
        // 将点赞行为发到mq，使用syncSendOrderly用以将同一个用户同一个评论的点赞行为发送到同一个队列中，以防对同一评论多次点赞的并发问题
        val sendResult = rocketMQTemplate.syncSendOrderly(CommentLikeTopic, dto, commentId + ":" + userId);
        if (sendResult == null || !sendResult.getSendStatus().equals(SendStatus.SEND_OK)) {
            log.error("likeOrCancelLike加入mq失败:{}", sendResult);
            throw new ApiException(type == 0 ? BlogResultCode.COMMENT_LIKE_ERROR : BlogResultCode.COMMENT_CANCEL_LIKE_ERROR);
        }
    }

    /**
     * 获取一级评论分页，带两条二级评论（如果有二级评论的话）
     */
    @Override
    public PageResult<CommentDTO> pageOneLevelComment(CommentPageDTO dto) {
        val articleEntity = articleService.getById(dto.getArticleId());
        if (articleEntity == null) {
            throw new ApiException(BlogResultCode.ARTICLE_NOT_EXIST);
        }
        // 先获取一级评论
        val oneLevelCommentPage = commentService.oneLevelCommentPage(dto, dto.getArticleId(), dto.getSort());
        if (oneLevelCommentPage.getRecords().isEmpty()) {
            return oneLevelCommentPage;
        }

        // 根据一级评论获取两条二级评论
        List<CommentDTO> twoLevelCommentList = commentService.getTwoLevelCommentForOneLevelComment(
                oneLevelCommentPage.getRecords().stream().map(CommentDTO::getId).collect(Collectors.toList()), 2);
        if (twoLevelCommentList.isEmpty()) {
            return oneLevelCommentPage;
        }
        Map<Long, List<CommentDTO>> groupedTwoLevel = twoLevelCommentList.stream()
                .collect(Collectors.groupingBy(CommentDTO::getParentId));
        // 组装
        oneLevelCommentPage.getRecords().forEach(it -> {
            val twoList = groupedTwoLevel.get(it.getId());
            if (twoList != null && !twoList.isEmpty()) {
                it.setChildren(twoList);
                it.setHasMoreChildren(it.getChildrenTotalCount() > it.getChildren().size());
            }
        });
        return oneLevelCommentPage;
    }

    @Override
    public PageResult<CommentDTO> pageTwoLevelComment(CommentPageDTO dto) {
        return commentService.twoLevelCommentPage(dto, dto.getArticleId(), dto.getOneLevelCommentId());
    }

}

