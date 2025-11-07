package com.wenziyue.blog.biz.mq.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wenziyue.blog.dal.dto.NotifyOutboxMqDTO;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.wenziyue.blog.common.constants.RedisConstant.NOTIFY_OUTBOX_HAS_SENT_KEY;
import static com.wenziyue.blog.common.constants.RocketMqTopic.NotifyOutboxTopic;

/**
 * @author wenziyue
 */
@Slf4j
@RequiredArgsConstructor
@Component
@RocketMQMessageListener(topic = NotifyOutboxTopic, consumerGroup = "notify-outbox-consumer-group",
        consumeMode = ConsumeMode.ORDERLY, // 消费模式，默认并发模式，消费顺序消息使用顺序模式
        maxReconsumeTimes = 2, consumeThreadNumber = 4, // 设置线程数为4,因为是顺序消费，所以和topic的读队列数对应，它默认为4
        enableMsgTrace = true)
public class NotifyOutboxListener implements RocketMQListener<MessageExt> {

    private final ObjectMapper objectMapper;
    private final RedisUtils redisUtils;

    @Override
    public void onMessage(MessageExt msg) {
        String keys = msg.getKeys(); // 生产端用 RocketMQHeaders.KEYS 设置的
        // 防止消息重复发送
        String dedupKey = NOTIFY_OUTBOX_HAS_SENT_KEY + keys;
        if (!redisUtils.setIfAbsentAndExpire(dedupKey, "1", 1, TimeUnit.DAYS)) {
            log.warn("消息已发送：{}", keys);
            return;
        }

        try {
            // 反序列化
            String body = new String(msg.getBody(), StandardCharsets.UTF_8);
            NotifyOutboxMqDTO dto = objectMapper.readValue(body, NotifyOutboxMqDTO.class);
            // 模拟发送消息
            log.info("向{}发送消息：用户{}点赞了您的评论{}", dto.getReceiverId(), dto.getUserId(), dto.getCommentId());
        } catch (Exception e) {
            // 失败释放占坑，允许 MQ 重投后重试
            redisUtils.delete(dedupKey);
            log.error("处理消息异常：{}", msg, e);
            throw new RuntimeException(e); // 让 RocketMQ 触发重试
        }
    }
}
