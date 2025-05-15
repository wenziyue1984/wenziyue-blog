package com.wenziyue.blog.common.utils;

import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 全局唯一ID生成器
 *
 * @author wenziyue
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IdGeneratorUtil {

    private final StringRedisTemplate stringRedisTemplate;

    // id初始值
    private static final long INITIAL_VALUE = 100000000000L;

    public long nextId() {
        // 如果 key 不存在，则尝试原子性设置初始值
        Boolean setSuccess = stringRedisTemplate.opsForValue()
                .setIfAbsent(RedisConstant.ID_GENERATOR_KEY, String.valueOf(INITIAL_VALUE));
        if (Boolean.TRUE.equals(setSuccess)) {
            log.info("全局自增id的key不存在，设置初始值:100000000000");
            return INITIAL_VALUE;
        }

        // 自增 1 并返回
        Long incrementedValue = stringRedisTemplate.opsForValue().increment(RedisConstant.ID_GENERATOR_KEY);
        if (incrementedValue == null) {
            log.error("获取Redis自增id失败");
            throw new IllegalStateException("获取Redis自增id失败");
        }
        return incrementedValue;
    }
}
