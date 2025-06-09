package com.wenziyue.blog.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.utils.SecurityUtils;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.common.exception.BlogResultCode;
import com.wenziyue.blog.dal.dto.LoginDTO;
import com.wenziyue.blog.biz.service.AuthService;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.blog.dal.service.UserService;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.security.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author wenziyue
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserService  userService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtils redisUtils;
    @Value("${wenziyue.security.expire}")
    private Long expire;
    @Value("${wenziyue.security.token-prefix}")
    private String tokenPrefix;

    @Override
    public String login(LoginDTO dto) {
        if (dto == null) {
            throw new ApiException(BlogResultCode.LOGIN_PARAM_IS_EMPTY);
        }
        // 校验验证码
//        if (dto.getCaptchaCode() == null || dto.getCaptchaCode().isEmpty()
//                || dto.getCaptchaUuid() == null || dto.getCaptchaUuid().isEmpty()) {
//            throw new ApiException(BlogResultCode.CAPTCHA_IS_EMPTY);
//        }
//        val captcha = redisUtils.get(RedisConstant.CAPTCHA_KEY + dto.getCaptchaUuid(), String.class);
//        if (captcha == null) {
//            throw new ApiException(BlogResultCode.CAPTCHA_HAS_EXPIRED);
//        }
//        if (!dto.getCaptchaCode().equals(captcha)) {
//            throw new ApiException(BlogResultCode.CAPTCHA_IS_ERROR);
//        }
//        // 删除校验通过的验证码
//        redisUtils.delete(RedisConstant.CAPTCHA_KEY + dto.getCaptchaUuid());

        // 校验登录信息
        if (dto.getName() == null || dto.getName().isEmpty()
                || dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new ApiException(BlogResultCode.REGISTER_PARAM_ERROR);
        }
        // 校验用户名密码
        val userEntity = userService.getOne(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getName, dto.getName()));
        if (userEntity == null) {
            log.error("login用户不存在：{}", dto);
            throw new ApiException(BlogResultCode.LOGIN_PARAM_ERROR);
        }
        if (!passwordEncoder.matches(dto.getPassword(), userEntity.getPassword())) {
            log.error("login用户密码不对：{}", dto);
            throw new ApiException(BlogResultCode.LOGIN_PARAM_ERROR);
        }
        // 生成token
        val token = jwtUtils.generateToken(userEntity.getId().toString());

        // 将用户信息存入redis
        SecurityUtils.userInfoSaveInRedis(redisUtils, userEntity, token, expire);
        // 维护用户的所有活跃token
        redisUtils.sAddAndExpire(RedisConstant.USER_TOKENS_KEY + userEntity.getId(), expire, TimeUnit.MILLISECONDS, token);
        return token;
    }

    @Override
    public String googleLogin(LoginDTO dto) {
        return null;
    }

    /**
     * 将redis中的用户信息作为token是否有效的依据，如果存在则认为token有效，否则认为token无效
     */
    @Override
    public boolean logout(String authorization) {
        if (authorization == null || !authorization.startsWith(tokenPrefix) || authorization.length() <= tokenPrefix.length() + 1) {
            return false;
        }
        String token = authorization.substring(tokenPrefix.length() + 1);
        // 删除redis中的用户信息
        redisUtils.delete(RedisConstant.LOGIN_TOKEN_KEY + token);
        // 解析用户id
        val userIdFromToken = jwtUtils.getUserIdFromToken(token);
        // 维护用户的所有活跃token
        redisUtils.sRemove(RedisConstant.USER_TOKENS_KEY + userIdFromToken, token);
        return true;
    }

    @Override
    public void forceLogout(Long id) {
        val tokenSet = redisUtils.sMembers(RedisConstant.USER_TOKENS_KEY + id);
        if (tokenSet == null || tokenSet.isEmpty()) {
            return;
        }
        tokenSet.forEach(token -> redisUtils.delete(RedisConstant.LOGIN_TOKEN_KEY + token.toString()));
        redisUtils.delete(RedisConstant.USER_TOKENS_KEY + id);
    }
}
