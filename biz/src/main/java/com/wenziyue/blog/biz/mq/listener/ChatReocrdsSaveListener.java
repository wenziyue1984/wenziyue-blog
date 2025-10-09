package com.wenziyue.blog.biz.mq.listener;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.service.BizChatService;
import com.wenziyue.blog.biz.utils.IdUtils;
import com.wenziyue.blog.common.utils.BlogUtils;
import com.wenziyue.blog.dal.dto.ChatRecordsSaveMqDTO;
import com.wenziyue.blog.dal.entity.ChatRecordEntity;
import com.wenziyue.blog.dal.entity.ChatSessionEntity;
import com.wenziyue.blog.dal.entity.ChatSessionStatusEntity;
import com.wenziyue.blog.dal.service.ChatRecordService;
import com.wenziyue.blog.dal.service.ChatSessionService;
import com.wenziyue.blog.dal.service.ChatSessionStatusService;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.uid.core.IdGen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQPushConsumerLifecycleListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.wenziyue.blog.common.constants.RedisConstant.CHAT_SESSION_MSG_LAST_TIME_KEY;
import static com.wenziyue.blog.common.constants.RocketMqTopic.ChatRecordsSaveTopic;

/**
 * @author wenziyue
 */
@Slf4j
@RequiredArgsConstructor
@Component
@RocketMQMessageListener(topic = ChatRecordsSaveTopic, consumerGroup = "chat-records-save-consumer-group",
        consumeMode = ConsumeMode.ORDERLY, // 消费模式，默认并发模式，消费顺序消息使用顺序模式
        maxReconsumeTimes = 2, consumeThreadNumber = 4, // 设置线程数为4,因为是顺序消费，所以和topic的读队列数对应，它默认为4
        enableMsgTrace = true)
public class ChatReocrdsSaveListener implements RocketMQListener<ChatRecordsSaveMqDTO>, RocketMQPushConsumerLifecycleListener {

    private final ChatSessionService chatSessionService;
    private final ChatRecordService chatRecordService;
    private final ChatSessionStatusService chatSessionStatusService;
    private final RedisUtils redisUtils;
    private final IdGen idGen;
    private final TransactionTemplate transactionTemplate;
    private final BizChatService bizChatService;
    private final RedisScript<Long> getNextSeqIfPresent;
    private final RedisScript<List> nextOrElectRebuild;
    private final RedisScript<Long> initFromBaseAndNext;
    private final RedisScript<List> lastOrElectRebuild;
    private final RedisScript<Long> initFromBase;
    private final StringRedisTemplate stringRedisTemplate;

    private volatile DefaultMQPushConsumer consumer;
    private final AtomicInteger inflight = new AtomicInteger(0);// 正在处理的消息数，用于优雅停机

    @Override
    public void prepareStart(DefaultMQPushConsumer consumer) {
        this.consumer = consumer; // 保存引用，之后可暂停/关闭
    }

    /**
     * 优雅停机，等待所有消息消费完成再停机，其中可调节 deadline 最多等待时间
     */
    @PreDestroy
    public void shutdown() {
        if (consumer != null) {
            try {
                consumer.suspend();
            } catch (Throwable ignore) {
            }
            // 等在途消费完成，最多等待时长 deadline
            long deadline = System.currentTimeMillis() + 10_000; // 10s 你可调
            while (inflight.get() > 0 && System.currentTimeMillis() < deadline) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            try {
                consumer.shutdown();
            } catch (Throwable e) {
                log.warn("consumer shutdown err", e);
            }
        }
    }

    @Override
    @Transactional
    public void onMessage(ChatRecordsSaveMqDTO dto) {
        inflight.incrementAndGet();
        try {
            if (dto == null || dto.getFromUserId() == null || dto.getToUserId() == null || dto.getContent() == null) {
                log.error("参数错误:{}", dto);
                return;
            }
            saveToDb(dto);
        } finally {
            inflight.decrementAndGet();
        }
    }

    /**
     * 将聊天记录保存到数据库
     */
    private void saveToDb(ChatRecordsSaveMqDTO dto) {
        // 获取聊天会话
        // - 先尝试从缓存中获取
        Long toUserId = dto.getToUserId();
        Long fromUserId = dto.getFromUserId();
        String sessionKey = BlogUtils.getChatSessionKey(fromUserId, toUserId);
        Long sessionId = redisUtils.get(sessionKey, Long.class);
        boolean updateLastReadSeq = true; // 是否需要更新最后阅读的seq
        if (sessionId == null) {
            // 从数据库获取会话id，如果没有会话，则创建一个会话，然后存入redis
            val idArray = BlogUtils.compareIdSize(fromUserId, toUserId);
            ChatSessionEntity chatSessionEntity = chatSessionService.getOne(Wrappers.<ChatSessionEntity>lambdaQuery()
                    .eq(ChatSessionEntity::getSmallUserId, idArray[0])
                    .eq(ChatSessionEntity::getBigUserId, idArray[1]));
            if (chatSessionEntity == null) {
                chatSessionEntity = ChatSessionEntity.builder()
                        .id(IdUtils.getID(idGen))
                        .smallUserId(idArray[0])
                        .bigUserId(idArray[1])
                        .build();
                try {
                    chatSessionService.save(chatSessionEntity);
                    sessionId = chatSessionEntity.getId();
                    redisUtils.set(sessionKey, sessionId, 30L, TimeUnit.DAYS);
                } catch (DuplicateKeyException e) {
                    // 其他线程/机器刚刚插入了，再查一次
                    chatSessionEntity = chatSessionService.getOne(Wrappers.<ChatSessionEntity>lambdaQuery()
                            .eq(ChatSessionEntity::getSmallUserId, idArray[0])
                            .eq(ChatSessionEntity::getBigUserId, idArray[1]));
                    sessionId = chatSessionEntity.getId();
                }
                // 创建聊天状态
                try {
                    chatSessionStatusService.save(ChatSessionStatusEntity.builder()
                            .sessionId(sessionId)
                            .userId(toUserId)
                            .otherUserId(fromUserId)
                            .lastReadSeq(0L)
                            .build());
                    chatSessionStatusService.save(ChatSessionStatusEntity.builder()
                            .sessionId(sessionId)
                            .userId(fromUserId)
                            .otherUserId(toUserId)
                            .lastReadSeq(dto.getSeq())
                            .build());
                    updateLastReadSeq = false; // 避免重复更新
                } catch (DuplicateKeyException e) {
                    // 唯一键可能是两个用户恰好同时创建会话，说明另一个用户已经创建，忽略即可
                    log.error("聊天状态表唯一键冲突,sessionId:{},fromUserId:{},toUserId:{}", sessionId, fromUserId, toUserId);
                }
            } else {
                sessionId = chatSessionEntity.getId();
                redisUtils.set(sessionKey, sessionId, 30L, TimeUnit.DAYS);
            }
        } else {
            // 延长缓存时间
            redisUtils.expire(sessionKey, 30L, TimeUnit.DAYS);
        }

        // 保存聊天记录
        chatRecordService.save(ChatRecordEntity.builder()
                .sessionId(sessionId)
                .content(dto.getContent())
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .timestamp(dto.getTimestamp())
                .seq(dto.getSeq())
                .build());

        if (updateLastReadSeq) {
            chatSessionStatusService.update(Wrappers.<ChatSessionStatusEntity>lambdaUpdate()
                    .set(ChatSessionStatusEntity::getLastReadSeq, dto.getSeq())
                    .eq(ChatSessionStatusEntity::getSessionId, sessionId)
                    .eq(ChatSessionStatusEntity::getUserId, fromUserId));
        }

        // 更新未读会话消息时间
        redisUtils.zAdd(CHAT_SESSION_MSG_LAST_TIME_KEY + toUserId, sessionId.toString(), System.currentTimeMillis());
    }


}
