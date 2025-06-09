package com.wenziyue.blog.biz.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wenziyue.blog.biz.security.TokenExpireDTO;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @author wenziyue
 */
@Slf4j
public class SecurityUtils {

    /**
     * 用户信息存入redis
     */
    public static void userInfoSaveInRedis(RedisUtils redisUtils, UserEntity userEntity, String token, Long expire) {
        redisUtils.set(RedisConstant.LOGIN_TOKEN_KEY + token, userEntity, expire, TimeUnit.MILLISECONDS);
    }

    public static String getTokenFromRequest(HttpServletRequest request, String tokenHeader, String tokenPrefix) {
        String authorization = request.getHeader(tokenHeader);
        if (authorization == null || !authorization.startsWith(tokenPrefix) || authorization.length() <= tokenPrefix.length() + 1) {
            log.warn("无效token:{}", authorization);
            return null;
        }
        return authorization.substring(tokenPrefix.length() + 1);
    }

    /**
     * 维护活跃的token
     */
    public static void refreshUserToken(RedisUtils redisUtils, String newToken, String oldToken, Long expire, Long userId, ObjectMapper objectMapper) {
        val sMembers = redisUtils.sMembers(RedisConstant.USER_TOKENS_KEY + userId);
        if (sMembers != null && !sMembers.isEmpty()) {
            sMembers.forEach(dto -> {
                val tokenExpireDTO = objectMapper.convertValue(dto, TokenExpireDTO.class);
                if (System.currentTimeMillis() > tokenExpireDTO.getExpireTimeStamp() || tokenExpireDTO.getToken().equals(oldToken)) {
                    redisUtils.sRemove(RedisConstant.USER_TOKENS_KEY + userId, tokenExpireDTO);
                }
            });
        }
        val tokenExpireDTO = TokenExpireDTO.builder().token(newToken).expireTimeStamp(System.currentTimeMillis() + expire).build();
        redisUtils.sAddAndExpire(RedisConstant.USER_TOKENS_KEY + userId, expire, TimeUnit.MILLISECONDS, tokenExpireDTO);
    }

    /**
     * 用户信息存入redis，并且维护用户的活跃token
     */
    public static void userInfoSaveInRedisAndRefreshUserToken(RedisUtils redisUtils, UserEntity userEntity, String newToken, String oldToken, Long expire, ObjectMapper objectMapper) {
        userInfoSaveInRedis(redisUtils, userEntity, newToken, expire);
        refreshUserToken(redisUtils, newToken, oldToken, expire, userEntity.getId(), objectMapper);
    }

}
