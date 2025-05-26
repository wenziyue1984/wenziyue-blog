package com.wenziyue.blog.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.dao.UserPageDTO;
import com.wenziyue.blog.biz.service.BizUserService;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.blog.dal.service.UserService;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.mybatisplus.page.PageResult;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.uid.common.Status;
import com.wenziyue.uid.core.IdGen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wenziyue
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BizUserServiceImpl implements BizUserService {

    private final UserService userService;
    private final RedisUtils redisUtils;
    private final IdGen idGen;

    @Override
    public List<UserEntity> queryUserList() {
        return userService.list();
    }

    @Override
    public UserEntity queryUserById(Long id) {
        val po = userService.getById(id);
        if (po == null) {
            throw new ApiException("800", "无此用户");
        }
        return po;
    }

    @Override
    public PageResult<UserEntity> pageUser(UserPageDTO dto) {
        return userService.page(dto, Wrappers.<UserEntity>lambdaQuery()
                .eq(dto.getId() != null, UserEntity::getId, dto.getId())
                .like(dto.getName() != null, UserEntity::getName, dto.getName())
                .like(dto.getEmail() != null, UserEntity::getEmail, dto.getEmail())
                .orderByDesc(UserEntity::getUpdateTime));
    }

    @Override
    public Long testUid() {
        val id = idGen.nextId();
        log.info("id:{}", id);
        return id.getId();
    }

    /**
     * uid-starter  压力测试
     *
     * @throws Exception e
     */
    @Override
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
}
