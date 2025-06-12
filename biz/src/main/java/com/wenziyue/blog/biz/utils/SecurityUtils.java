package com.wenziyue.blog.biz.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wenziyue.blog.biz.security.TokenExpireDTO;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author wenziyue
 */
@Slf4j
public class SecurityUtils {

    /**
     * 用户信息存入redis
     */
    public static void saveUserInfoInRedis(RedisUtils redisUtils, UserEntity userEntity, String token, Long expire) {
        redisUtils.set(RedisConstant.LOGIN_TOKEN_KEY + token, userEntity, expire, TimeUnit.SECONDS);
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
     * 维护活跃的token集合
     */
    public static void refreshAndAddUserActiveTokenSet(RedisUtils redisUtils, String newToken, String oldToken, Long expire, Long userId, ObjectMapper objectMapper) {
        refreshUserActiveTokenSet(redisUtils, userId, oldToken, objectMapper);
        val tokenExpireDTO = TokenExpireDTO.builder().token(newToken).expireTimeStamp(System.currentTimeMillis() + (expire * 1000)).build();
        redisUtils.sAddAndExpire(RedisConstant.USER_TOKENS_KEY + userId, expire, TimeUnit.SECONDS, tokenExpireDTO);
    }

    /**
     * 更新用户活跃token集合
     *
     * @return 返回刷新后的活跃token集合
     */
    public static Set<Object> refreshUserActiveTokenSet(RedisUtils redisUtils, Long userId, String oldToken, ObjectMapper objectMapper) {
        val sMembers = redisUtils.sMembers(RedisConstant.USER_TOKENS_KEY + userId);
        if (sMembers == null || sMembers.isEmpty()) {
            return null;
        }

        Set<Object> result = new HashSet<>();
        for (Object dto : sMembers) {
            val tokenExpireDTO = objectMapper.convertValue(dto, TokenExpireDTO.class);
            if (System.currentTimeMillis() > tokenExpireDTO.getExpireTimeStamp() || tokenExpireDTO.getToken().equals(oldToken)) {
                redisUtils.sRemove(RedisConstant.USER_TOKENS_KEY + userId, dto);
                // 删除失效token，其实不是必须的，因为redis会自动删除过期的key，但是为了安全起见，还是删除了
                redisUtils.delete(RedisConstant.LOGIN_TOKEN_KEY + tokenExpireDTO.getToken());
            } else {
                result.add(dto);
            }
        }
        return result;
    }

    /**
     * 用户信息存入redis，并且维护用户的活跃token
     */
    public static void userInfoSaveInRedisAndRefreshUserToken(RedisUtils redisUtils, UserEntity userEntity, String newToken, String oldToken, Long expire, ObjectMapper objectMapper) {
        saveUserInfoInRedis(redisUtils, userEntity, newToken, expire);
        refreshAndAddUserActiveTokenSet(redisUtils, newToken, oldToken, expire, userEntity.getId(), objectMapper);
    }

    /**
     * 修改用户信息后同步redis中保存的用户信息
     */
    public static void refreshUserInfo(RedisUtils redisUtils, UserEntity userEntity, ObjectMapper objectMapper) {
        val sMembers = refreshUserActiveTokenSet(redisUtils, userEntity.getId(), null, objectMapper);
        if (sMembers == null || sMembers.isEmpty()) {
            return;
        }
        sMembers.forEach(dto -> {
            val tokenExpireDTO = objectMapper.convertValue(dto, TokenExpireDTO.class);
            val key = RedisConstant.LOGIN_TOKEN_KEY + tokenExpireDTO.getToken();
            val expire = redisUtils.getExpire(key);
            if (expire > 0) {
                redisUtils.set(key, userEntity, expire, TimeUnit.SECONDS);
            } else if (expire == -1) {
                // 与原先的过期时间保持一致，如果原先token不过期这里也设置不过期
                redisUtils.set(key, userEntity);
            }
        });
    }

    /**
     * 清除用户所有登录token
     */
    public static void clearUserAllToken(RedisUtils redisUtils, Long userId, ObjectMapper objectMapper) {
        String key = RedisConstant.USER_TOKENS_KEY + userId;
        val sMembers = redisUtils.sMembers(key);
        if (sMembers == null || sMembers.isEmpty()) {
            return;
        }
        sMembers.forEach(dto -> {
            val tokenExpireDTO = objectMapper.convertValue(dto, TokenExpireDTO.class);
            redisUtils.delete(RedisConstant.LOGIN_TOKEN_KEY + tokenExpireDTO.getToken());
        });
        redisUtils.delete(key);
    }

}
