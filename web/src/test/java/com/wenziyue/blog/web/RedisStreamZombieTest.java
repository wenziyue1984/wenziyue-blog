//package com.wenziyue.blog.web;
//
//import com.wenziyue.blog.biz.handler.ArticleLikeBufferHandler;
//import com.wenziyue.blog.web.config.DotenvInitializer;
//import com.wenziyue.redis.utils.RedisUtils;
//import lombok.extern.slf4j.Slf4j;
//import lombok.val;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.connection.stream.MapRecord;
//import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.ContextConfiguration;
//
//import java.time.Duration;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.concurrent.TimeUnit;
//
//import static com.wenziyue.blog.common.constants.RedisConstant.ARTICLE_LIKE_STREAM_GROUP_NAME;
//import static com.wenziyue.blog.common.constants.RedisConstant.ARTICLE_LIKE_STREAM_KEY;
//
//
///**
// * @author wenziyue
// */
//@SpringBootTest(classes = TestApplication.class, properties = "like.worker.enabled=false")
//@Slf4j
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@ActiveProfiles("test")
////@Import(DotenvInitializer.class)
//@ContextConfiguration(initializers = DotenvInitializer.class)
//public class RedisStreamZombieTest {
//
//    @Autowired
//    RedisUtils redisUtils;
//    @Autowired
//    ArticleLikeBufferHandler handler;   // 需要 @ComponentScan 能扫到
//    private static final String CONSUMER = "zombie_reader";
//
//    private static String zombieId;
//
//    @BeforeAll
//    static void init(@Autowired RedisUtils redis) {
//        redis.delete(ARTICLE_LIKE_STREAM_KEY);
//        redis.xGroupCreate(ARTICLE_LIKE_STREAM_KEY, ARTICLE_LIKE_STREAM_GROUP_NAME);
//    }
//
//    /**
//     * 1️⃣ 写入一条消息 & 读走但不 ACK
//     */
//    @Test
//    @Order(1)
//    void produceAndRead() {
//        val map = new HashMap<String, String>();
//        map.put("userId", String.valueOf(1));
//        map.put("articleId", String.valueOf(2));
//        map.put("time", String.valueOf(1));
//        map.put("type", String.valueOf(1));
//        zombieId = redisUtils.xAdd(ARTICLE_LIKE_STREAM_KEY, map);
//        // 拉走但不 ack
//        redisUtils.xReadGroup(ARTICLE_LIKE_STREAM_KEY, ARTICLE_LIKE_STREAM_GROUP_NAME, CONSUMER, 1, Duration.ofSeconds(1));
//        Assertions.assertNotNull(zombieId);
//        log.info("制造僵尸消息 {}", zombieId);
//    }
//
//    /**
//     * 2️⃣ 等 >60s，把它变成僵尸（这里用 Thread.sleep() 省事；真实单元测试可用 @Testcontainers + fast-forward）
//     */
//    @Test
//    @Order(2)
//    void waitForIdle() throws InterruptedException {
//        Thread.sleep(61_000);
//    }
//
//    /**
//     * 3️⃣ 手动触发 reclaim 逻辑（无需等调度器）
//     */
//    @Test
//    @Order(3)
//    void manualReclaim() {
//        handler.reclaimDeadMessages();   // 直接调用
//    }
//
//    /**
//     * 4️⃣ 验证：队列里确实有那条消息
//     */
//    @Test
//    @Order(4)
//    void queueShouldContainZombie() throws InterruptedException {
//        // poll 最多 2 秒等待消费线程捞走
//        MapRecord<String, ?, ?> rec = handler.getQueue().poll(2, TimeUnit.SECONDS);
//        Assertions.assertNotNull(rec, "queue 空，reclaim 失败?");
//        Assertions.assertEquals(zombieId, rec.getId().getValue(), "不是那条僵尸消息");
//        // 模拟消费掉消息
//        log.info("模拟重新消费了消息 {}", rec.getValue());
//        redisUtils.xAck(ARTICLE_LIKE_STREAM_KEY, ARTICLE_LIKE_STREAM_GROUP_NAME, Collections.singletonList(rec.getId().getValue()));
//        redisUtils.xDel(ARTICLE_LIKE_STREAM_KEY, Collections.singletonList(rec.getId().getValue()));
//    }
//
//    /**
//     * 5️⃣ 等消费线程批量 flush，然后确认 PENDING 已清空
//     */
//    @Test
//    @Order(5)
//    void pendingShouldBeZero() throws InterruptedException {
//        Thread.sleep(2_500);  // FLUSH_INTERVAL_MS = 2000
//        PendingMessagesSummary sum = redisUtils.xPendingSummary(ARTICLE_LIKE_STREAM_KEY, ARTICLE_LIKE_STREAM_GROUP_NAME);
//        Assertions.assertEquals(0, sum.getTotalPendingMessages(), "还有未确认消息，ACK 失败?");
//    }
//
//    @AfterAll
//    static void clean(@Autowired RedisUtils redis) {
//        redis.delete(ARTICLE_LIKE_STREAM_KEY);
//    }
//}
