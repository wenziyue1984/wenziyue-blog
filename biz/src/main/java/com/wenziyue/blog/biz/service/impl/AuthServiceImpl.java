package com.wenziyue.blog.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.common.enums.UserRoleEnum;
import com.wenziyue.blog.common.enums.UserStatusEnum;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
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
    private Long expire = 604800000L;

    @Override
    public String login(LoginDTO dto) {
        if (dto == null) {
            throw new ApiException(BlogResultCode.LOGIN_PARAM_IS_EMPTY);
        }
        // 校验验证码
        if (dto.getCaptchaCode() == null || dto.getCaptchaCode().isEmpty()
                || dto.getCaptchaUuid() == null || dto.getCaptchaUuid().isEmpty()) {
            throw new ApiException(BlogResultCode.CAPTCHA_IS_EMPTY);
        }
        val captcha = redisUtils.get(RedisConstant.CAPTCHA_KEY + dto.getCaptchaUuid(), String.class);
        if (captcha == null) {
            throw new ApiException(BlogResultCode.CAPTCHA_HAS_EXPIRED);
        }
        if (!dto.getCaptchaCode().equals(captcha)) {
            throw new ApiException(BlogResultCode.CAPTCHA_IS_ERROR);
        }
        // 删除校验通过的验证码
        redisUtils.delete(RedisConstant.CAPTCHA_KEY + dto.getCaptchaUuid());

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

        // 将用户信息存入redis
        // 角色权限(简易版本，如有需要可扩充)
        List<GrantedAuthority> authorities = Collections.singletonList((GrantedAuthority) () ->
                userEntity.getRole().equals(UserRoleEnum.ADMIN) ?  UserRoleEnum.ADMIN.getRole() : UserRoleEnum.USER.getRole());
        val user = User.builder()
                .username(userEntity.getName())
                .password(userEntity.getPassword()) // 密文
                .authorities(authorities) // 授权列表
                .accountLocked(userEntity.getStatus().equals(UserStatusEnum.DISABLED)) // 禁用/锁定
                .build();
        redisUtils.set(RedisConstant.LOGIN_TOKEN_KEY + userEntity.getId(), user, expire, TimeUnit.MILLISECONDS);

        // 生成token返回
        return jwtUtils.generateToken(userEntity.getId().toString());
    }

    @Override
    public String googleLogin(LoginDTO dto) {
        return null;
    }
}
