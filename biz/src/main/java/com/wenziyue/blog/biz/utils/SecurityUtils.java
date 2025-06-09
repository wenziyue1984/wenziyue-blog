package com.wenziyue.blog.biz.utils;

import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.common.enums.UserRoleEnum;
import com.wenziyue.blog.common.enums.UserStatusEnum;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.redis.utils.RedisUtils;
import lombok.val;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wenziyue
 */
public class SecurityUtils {

    /**
     * 用户信息存入redis
     */
    public static void userInfoSaveInRedis(RedisUtils redisUtils, UserEntity userEntity, String token, Long expire) {
        // 角色权限(简易版本，如有需要可扩充)
        List<GrantedAuthority> authorities = Collections.singletonList((GrantedAuthority) () ->
                userEntity.getRole().equals(UserRoleEnum.ADMIN) ?  UserRoleEnum.ADMIN.getRole() : UserRoleEnum.USER.getRole());
        val user = User.builder()
                .username(userEntity.getName())
                .password(userEntity.getPassword()) // 密文
                .authorities(authorities) // 授权列表
                .accountLocked(userEntity.getStatus().equals(UserStatusEnum.DISABLED)) // 禁用/锁定
                .build();
        redisUtils.set(RedisConstant.LOGIN_TOKEN_KEY + token, user, expire, TimeUnit.MILLISECONDS);
    }

}
