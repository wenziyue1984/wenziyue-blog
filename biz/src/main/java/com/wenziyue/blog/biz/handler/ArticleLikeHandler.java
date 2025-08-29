package com.wenziyue.blog.biz.handler;

import com.wenziyue.blog.common.enums.LikeTypeEnum;
import com.wenziyue.blog.dal.entity.ArticleLikeEntity;
import com.wenziyue.blog.dal.service.ArticleLikeService;
import com.wenziyue.framework.utils.EnumUtils;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static com.wenziyue.blog.common.constants.RedisConstant.ARTICLE_LIKE_STREAM_GROUP_NAME;
import static com.wenziyue.blog.common.constants.RedisConstant.ARTICLE_LIKE_STREAM_KEY;

/**
 * 用于配合LikeStreamContainerConfig做点赞消息的入库处理，现已被弃用，由ArticleLikeBufferHandler替代
 *
 * @author wenziyue
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleLikeHandler implements StreamListener<String, MapRecord<String,String,String>> {

    private final ArticleLikeService likeService;
    private final RedisUtils redisUtils;

    @Override
    public void onMessage(MapRecord<String, String, String> record) {

        try {
            // 1) 转实体
            ArticleLikeEntity entity = ArticleLikeEntity.builder()
                    .articleId(Long.valueOf(record.getValue().get("articleId")))
                    .userId(Long.valueOf(record.getValue().get("userId")))
                    .time(Long.valueOf(record.getValue().get("time")))
                    .type(EnumUtils.fromCode(
                            LikeTypeEnum.class,
                            Integer.parseInt(record.getValue().get("type"))))
                    .build();

            likeService.save(entity);   // or accumulate & batch-save

            // 2) 手动 ACK（容器里有 connection）
            redisUtils.xAck(ARTICLE_LIKE_STREAM_KEY, ARTICLE_LIKE_STREAM_GROUP_NAME, record.getId().getValue());

            // 3) 可选：立刻删除，彻底免积压
            redisUtils.xDel(ARTICLE_LIKE_STREAM_KEY, Collections.singletonList(record.getId().getValue()));

        } catch (Exception e) {
            log.error("处理点赞日志失败, record={}", record, e);
            // 不 ack，让它进入 pending，稍后重试
        }
    }
}
