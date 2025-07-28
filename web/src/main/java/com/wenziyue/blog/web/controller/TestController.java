package com.wenziyue.blog.web.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.wenziyue.blog.biz.security.BlogUserDetails;
import com.wenziyue.blog.biz.service.impl.AsyncService;
import com.wenziyue.blog.common.enums.ThirdOauthProviderEnum;
import com.wenziyue.blog.common.exception.BlogResultCode;
import com.wenziyue.blog.dal.entity.ThirdOauthEntity;
import com.wenziyue.blog.dal.service.ThirdOauthService;
import com.wenziyue.framework.annotation.ResponseResult;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.framework.trace.MdcExecutors;
import com.wenziyue.framework.trace.MdcTaskDecorator;
import com.wenziyue.idempotent.annotation.WenziyueIdempotent;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.uid.common.Status;
import com.wenziyue.uid.core.IdGen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * @author wenziyue
 */
@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@ResponseResult
public class TestController {

    private final AsyncService asyncService;
//    private final MdcTaskDecorator mdcTaskDecorator;
    private final ExecutorService executorService;
    private final IdGen idGen;
    private final ThirdOauthService thirdOathService;
    private final RocketMQTemplate rocketMQTemplate;
    private final RedisUtils redisUtils;

    /**
     * 测试uid-starter
     */
    @GetMapping("/uid")
    public Long testUid() {
        val id = idGen.nextId();
        log.info("id:{}", id);
        return id.getId();
    }

    /**
     * 压测uid-starter
     */
    @GetMapping("/uidBenchMark")
    public void uidBenchMark() throws Exception {

        log.info("🚀 启动 Segment UID Benchmark...");
        int threadCount = 10;         // 并发线程数
        int idsPerThread = 1000000;   // 每个线程生成多少个 ID
        CountDownLatch latch = new CountDownLatch(threadCount); // 用于等待所有线程执行完毕
        ExecutorService pool = Executors.newFixedThreadPool(threadCount); // 固定线程池

        long start = System.currentTimeMillis(); // 记录开始时间
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {

                for (int j = 0; j < idsPerThread; j++) {
                    val result = idGen.nextId();// 调用 ID 生成方法（你实现的 segment）
                    if (result.getStatus().equals(Status.EXCEPTION)) {
//                        log.error("id失败:{}", result.getId());
                        failCount.incrementAndGet();
                    }
                }
                log.info("✅ 线程 {} 生成 {} 个 ID，失败 {} 个", Thread.currentThread().getName(), idsPerThread, failCount.get());
                latch.countDown(); // 当前线程完成后计数器减一
            });
        }

        latch.await(); // 主线程等待所有线程执行完毕
        long cost = System.currentTimeMillis() - start; // 记录耗时
        long total = (long) threadCount * idsPerThread;

        log.info("🚀 Segment UID Benchmark 完成：共生成 {} 个 ID，用时 {} ms，平均速率：{} ID/s, 失败了 {} 个id",
                total, cost, total * 1000 / cost / 10, failCount.get());
    }

    /**
     * 测试幂等注解
     */
    @GetMapping("/testIdempotent/{id}")
    @WenziyueIdempotent(keys = {"#id"}, prefix = "blog:idempotentTest", timeout = 60)
    public void testIdempotent(@PathVariable String id) {
        log.info("idempotentTest:{}", id);
    }

    /**
     * 测试异步方法log的traceId
     */
    @GetMapping(value = "/testasync")
    public String testAsync() {
        asyncService.asyncMethod();
        return "触发 @Async 方法";
    }

    /**
     * 测试CompletableFuture中log的traceId
     */
    @GetMapping("/testfuture")
    public String testCompletableFuture() {
        log.info("主线程启动任务");

//        ExecutorService executor = MdcExecutors.newFixedThreadPoolWithMdc(2, mdcTaskDecorator);

        CompletableFuture.runAsync(() -> {
            log.info("异步任务 A 执行中...");
        }, executorService);

        CompletableFuture.runAsync(() -> {
            log.info("异步任务 B 执行中...");
        }, executorService);

        return "CompletableFuture 异步任务已提交";
    }

    /**
     * 测试mysql的json类型
     */
    @GetMapping("/testMysqlJson")
    public String testmysqljson() {
        ThirdOauthEntity thirdOathEntity = new ThirdOauthEntity();
        val id = idGen.nextId().getId();
        thirdOathEntity.setId(id);
        thirdOathEntity.setUserId(1L);
        thirdOathEntity.setProvider(ThirdOauthProviderEnum.GITHUB);
        StringBuilder uidStringBuilder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            uidStringBuilder.append((int) (Math.random() * 10)); // 生成 0-9 的随机数
        }
        thirdOathEntity.setProviderUid(uidStringBuilder.toString());
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("wenziyue");
        payload.setEmail("test@gmail.com");
        thirdOathEntity.setExtra(payload);
        thirdOathService.save(thirdOathEntity);

        val entity = thirdOathService.getById(id);
        log.info("entity:{}", entity);
        String name = entity.getExtra().get("sub").toString();
        log.info("sub:{}", name);

        thirdOathService.removeById(id);
        return "测试成功";
    }

    @GetMapping("/testSecurityContextHolder")
    public String testSecurityContextHolder() {
        val user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("user:{}", user);

        BlogUserDetails blogUserDetails = user instanceof BlogUserDetails ? (BlogUserDetails) user : null;
        log.info("blogUserDetails:{}", blogUserDetails);
        val userEntity = blogUserDetails == null ? null : blogUserDetails.getUserEntity();
        log.info("userEntity:{}", userEntity);

        return blogUserDetails == null ? "null" : blogUserDetails.toString();
    }

    @GetMapping("/testRocketMqTopic")
    public void testRocketMqTopic() {
        val sendResult = rocketMQTemplate.syncSend("test_topic:default", MessageBuilder.withPayload("hello").build());
        log.info("sendResult:{}", sendResult);
        if (sendResult == null || !sendResult.getSendStatus().equals(SendStatus.SEND_OK)) {
            log.error("slug加入mq失败:{}", sendResult);
            throw new ApiException(BlogResultCode.SLUG_GENERATE_ERROR);
        }
    }

    @GetMapping("/testIncrementWithExpire")
    public void testIncrementWithExpire() throws Exception {
        redisUtils.increment("test:increment", 1L, 2L, TimeUnit.MINUTES);
    }

    @GetMapping("/testIncrement")
    public void testIncrement() throws Exception {
        redisUtils.increment("test:increment", 1);
    }

}
