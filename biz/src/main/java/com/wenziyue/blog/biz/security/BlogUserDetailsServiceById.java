package com.wenziyue.blog.biz.security;

import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.common.enums.UserRoleEnum;
import com.wenziyue.blog.common.enums.UserStatusEnum;
import com.wenziyue.blog.common.exception.BlogResultCode;
import com.wenziyue.blog.dal.service.UserService;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.security.service.UserDetailsServiceById;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wenziyue
 */
@Slf4j
@Service
@Primary // 确保覆盖 Starter 中的 defaultUserDetailsServiceById
@RequiredArgsConstructor
public class BlogUserDetailsServiceById implements UserDetailsServiceById {

    private final UserService userService;
    private final RedisUtils redisUtils;
    @Value("${wenziyue.security.expire}")
    private Long expire = 604800000L;

    /**
     * 根据id获取用户信息
     * 因为security-starter 中 JwtAuthenticationFilter.doFilterInternal() 在调用此方法时已经校验了token是否过期，所以这里认定token肯定没有过期
     */
    @Override
    public UserDetails loadUserById(String id) {
        //
        // 先从redis中获取用户信息
        val redisUser = redisUtils.get(RedisConstant.LOGIN_TOKEN_KEY + id, User.class);
        if (redisUser != null) {
            return redisUser;
        }

        log.warn("loadUserById redis中用户信息不存在:{}", id);
        // 获取用户信息
        val userEntity = userService.getById(Long.valueOf(id));
        if (userEntity == null) {
            log.error("用户不存在:{}", id);
            throw new ApiException(BlogResultCode.USER_NOT_EXIST);
        }
        // 角色权限(简易版本，如有需要可扩充)
        List<GrantedAuthority> authorities = Collections.singletonList((GrantedAuthority) () ->
                userEntity.getRole().equals(UserRoleEnum.ADMIN) ?  UserRoleEnum.ADMIN.getRole() : UserRoleEnum.USER.getRole());

        val user = User.builder()
                .username(userEntity.getName())
                .password(userEntity.getPassword()) // 密文
                .authorities(authorities) // 授权列表
                .accountLocked(userEntity.getStatus().equals(UserStatusEnum.DISABLED)) // 禁用/锁定
                .build();
        redisUtils.set(RedisConstant.LOGIN_TOKEN_KEY + id, user, expire, TimeUnit.MILLISECONDS);
        return user;
    }
}
