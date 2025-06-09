package com.wenziyue.blog.biz.security;

import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.security.service.UserDetailsServiceByIdOrToken;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Slf4j
@Service
@Primary // 确保覆盖 Starter 中的 defaultUserDetailsServiceById
@RequiredArgsConstructor
public class BlogUserDetailsServiceById implements UserDetailsServiceByIdOrToken {

    private final RedisUtils redisUtils;

    /**
     * 根据id从redis中获取用户信息，如果不存在则认为token无效
     * 因为security-starter 中 JwtAuthenticationFilter.doFilterInternal() 在调用此方法时已经校验了token是否过期，所以这里认定token肯定没有过期
     */
    @Override
    public UserDetails loadUserByUserIdOrToken(String id, String token) {
        // 先从redis中获取用户信息
        val redisUser = redisUtils.get(RedisConstant.LOGIN_TOKEN_KEY + token, User.class);
        if (redisUser == null) {
            log.error("redis中不存在登录的用户信息:{}", id);
            throw new JwtException("redis中不存在登录的用户信息:" + id);
        }
        // 如果该token已不在用户活跃token集合中，则视为无效token，并且删除redis中的用户信息
        if (!redisUtils.sIsMember(RedisConstant.USER_TOKENS_KEY + id, token)) {
            redisUtils.delete(RedisConstant.LOGIN_TOKEN_KEY + token);
            throw new JwtException("token已不再活跃集合中");
        }
        return redisUser;
    }
}
