package com.wenziyue.blog.biz.config;

import com.wenziyue.blog.biz.handler.ArticleLikeBufferHandler;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import javax.annotation.PreDestroy;
import java.time.Duration;

import static com.wenziyue.blog.common.constants.RedisConstant.ARTICLE_LIKE_STREAM_GROUP_NAME;
import static com.wenziyue.blog.common.constants.RedisConstant.ARTICLE_LIKE_STREAM_KEY;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LikeStreamContainerConfig {

    private final RedisConnectionFactory connectionFactory;
    private final RedisUtils redisUtils;
    private final ArticleLikeBufferHandler articleLikeBufferHandler;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> likeStreamListener() {

        // 确保组存在
        if (!redisUtils.hasKey(RedisConstant.ARTICLE_LIKE_STREAM_KEY)) {
            redisUtils.xGroupCreate(ARTICLE_LIKE_STREAM_KEY, ARTICLE_LIKE_STREAM_GROUP_NAME);
        }

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(5))
                        .batchSize(1000)
                        .build();

        container = StreamMessageListenerContainer.create(connectionFactory, options);

        // 每个实例用不同的 consumer-name（Pod/进程唯一）
        container.receive(
                Consumer.from(ARTICLE_LIKE_STREAM_GROUP_NAME, articleLikeBufferHandler.getConsumerName()),
                StreamOffset.create(ARTICLE_LIKE_STREAM_KEY, ReadOffset.lastConsumed()),
                articleLikeBufferHandler
        );

        container.start();
        return container;
    }

    @PreDestroy
    public void shutdown() {
        if (container != null) {
            container.stop();
        }
    }
}