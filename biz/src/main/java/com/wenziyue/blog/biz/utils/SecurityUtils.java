package com.wenziyue.blog.biz.utils;

import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;

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

}
