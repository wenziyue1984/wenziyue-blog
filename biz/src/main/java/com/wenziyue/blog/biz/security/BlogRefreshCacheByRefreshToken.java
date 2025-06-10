package com.wenziyue.blog.biz.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wenziyue.blog.biz.utils.SecurityUtils;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.common.exception.BlogResultCode;
import com.wenziyue.blog.dal.service.UserService;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.security.service.RefreshCacheByRefreshToken;
import com.wenziyue.security.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Slf4j
@Service
@Primary // 确保覆盖 Starter 中的 defaultRefreshCacheByRefreshToken
@RequiredArgsConstructor
public class BlogRefreshCacheByRefreshToken implements RefreshCacheByRefreshToken {

    private final RedisUtils redisUtils;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Value("${wenziyue.security.expire}")
    private Long expire;

    @Override
    public void refreshCacheByRefreshToken(String oldToken, String newToken) {
        try {
            // 解析用户信息
            val userIdFromToken = jwtUtils.getUserIdFromToken(oldToken);
            if (userIdFromToken == null) {
                log.error("refreshCacheByRefreshToken,token解析失败:{}", oldToken);
                throw new JwtException("token解析失败:" + oldToken);
            }

            // 删除原缓存
            redisUtils.delete(RedisConstant.LOGIN_TOKEN_KEY + oldToken);

            val userEntity = userService.getById(userIdFromToken);
            if (userEntity == null) {
                log.error("refreshCacheByRefreshToken用户不存在：{}", userIdFromToken);
                throw new ApiException(BlogResultCode.USER_NOT_EXIST);
            }

            // 将用户信息存入redis && 维护用户的所有活跃token
            SecurityUtils.userInfoSaveInRedisAndRefreshUserToken(redisUtils, userEntity, newToken, oldToken, expire, objectMapper);
        } catch (JwtException | ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("refreshCacheByRefreshToken刷新缓存异常", e);
            throw new ApiException(BlogResultCode.REFRESH_TOKEN_ERROR);
        }
    }

}
