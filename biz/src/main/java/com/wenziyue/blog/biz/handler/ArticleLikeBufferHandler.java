package com.wenziyue.blog.biz.handler;

import com.wenziyue.blog.common.enums.LikeTypeEnum;
import com.wenziyue.blog.dal.entity.ArticleLikeEntity;
import com.wenziyue.blog.dal.service.ArticleLikeService;
import com.wenziyue.framework.utils.EnumUtils;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.wenziyue.blog.common.constants.RedisConstant.ARTICLE_LIKE_STREAM_GROUP_NAME;
import static com.wenziyue.blog.common.constants.RedisConstant.ARTICLE_LIKE_STREAM_KEY;

/**
 * @author wenziyue
 */
@Component
@Slf4j
@RequiredArgsConstructor
@EnableScheduling
public class ArticleLikeBufferHandler implements StreamListener<String, MapRecord<String,String,String>>, InitializingBean {

    private final ArticleLikeService likeService;
    private final RedisUtils redisUtils;
    @Getter
    private final String consumerName = "like_worker-" + UUID.randomUUID();

    // 内部缓冲队列
    @Getter
    private final BlockingQueue<MapRecord<String, ?, ?>> queue = new LinkedBlockingQueue<>(5000);
    @Value("${like.worker.enabled:true}")   // 默认生产环境开启
    private boolean workerEnabled;

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        // 快速解析后放入队列
        val added = queue.offer(record);// offer 非阻塞，put 会阻塞
        if (!added) log.error("消息队列已满，丢弃消息:{}", record);
    }

    @Override
    public void afterPropertiesSet() {
        if (!workerEnabled) {
            log.info("检测到 like.worker.enabled=false，跳过后台线程启动（测试模式）");
            return;
        }
        // 启动后台处理线程
        Thread worker = new Thread(this::processLoop);
        worker.setName("article-like-worker-" + UUID.randomUUID());
        worker.setDaemon(true);
        worker.start();
    }

    private void processLoop() {
        List<ArticleLikeEntity> buffer = new ArrayList<>(1500);
        List<String> ids = new ArrayList<>();
        long FLUSH_INTERVAL_MS = 2000;
        long lastFlushTime = System.currentTimeMillis();

        while (true) {
            try {
                // 每次取一条，等待最多 1 秒
                MapRecord<String, ?, ?> r = queue.poll(1, TimeUnit.SECONDS);
                if (r != null) {
                    buffer.add(toEntity(r));
                    ids.add(r.getId().getValue());
                }

                // 满足批量写入条件（满1000条 或 刷新时间大于2秒）
                if (buffer.size() >= 1000 || (!buffer.isEmpty() && System.currentTimeMillis() - lastFlushTime >= FLUSH_INTERVAL_MS)) {
                    likeService.saveBatch(buffer);
                    redisUtils.xAck(ARTICLE_LIKE_STREAM_KEY, ARTICLE_LIKE_STREAM_GROUP_NAME, ids);
                    redisUtils.xDel(ARTICLE_LIKE_STREAM_KEY, ids);

                    buffer.clear();
                    ids.clear();
                    lastFlushTime = System.currentTimeMillis();
                }

            } catch (Exception e) {
                log.error("点赞批量写库失败", e);
            }
        }
    }

    private ArticleLikeEntity toEntity(MapRecord<String, ?, ?> record) {
        Map<?, ?> map = record.getValue();
        return ArticleLikeEntity.builder()
                .articleId(Long.valueOf(map.get("articleId").toString()))
                .userId(Long.valueOf(map.get("userId").toString()))
                .time(Long.valueOf(map.get("time").toString()))
                .type(EnumUtils.fromCode(LikeTypeEnum.class, Integer.parseInt(map.get("type").toString())))
                .build();
    }

    @Scheduled(fixedDelay = 60000)
    public void reclaimDeadMessages() {
        try {
            // 获取Pending超时未处理的消息（超过1分钟）
            List<PendingMessage> deadMessages = redisUtils.xPendingHead(
                            ARTICLE_LIKE_STREAM_KEY,
                            ARTICLE_LIKE_STREAM_GROUP_NAME,
                            "0",
                            1000
                    ).stream().filter(pm -> pm.getElapsedTimeSinceLastDelivery().toMillis() >= 60000).collect(Collectors.toList());

            if (deadMessages.isEmpty()) return;

            List<String> deadIds = deadMessages.stream().map(PendingMessage::getId).map(RecordId::getValue).collect(Collectors.toList());

            // 将这些消息 claim 到当前 consumer 名下
            List<MapRecord<String, Object, Object>> claimed = redisUtils.xClaim(
                    ARTICLE_LIKE_STREAM_KEY,
                    ARTICLE_LIKE_STREAM_GROUP_NAME,
                    consumerName,
                    Duration.ofMillis(60000),
                    deadIds
            );

            // 投放进队列等待处理
            for (MapRecord<String, ?, ?> record : claimed) {
                boolean ok = queue.offer(record);
                if (!ok) {
                    log.warn("claim 消息放入缓冲队列失败: {}", record);
                }
            }
            log.info("Claim并重新投放僵尸消息完成，共 {} 条", claimed.size());

        } catch (Exception e) {
            log.error("claim 僵尸消息失败", e);
        }
    }

}