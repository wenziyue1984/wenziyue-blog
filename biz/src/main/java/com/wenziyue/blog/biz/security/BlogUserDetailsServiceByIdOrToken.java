package com.wenziyue.blog.biz.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.common.enums.UserRoleEnum;
import com.wenziyue.blog.common.enums.UserStatusEnum;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.security.service.UserDetailsServiceByIdOrToken;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author wenziyue
 */
@Slf4j
@Service
@Primary // 确保覆盖 Starter 中的 defaultUserDetailsServiceById
@RequiredArgsConstructor
public class BlogUserDetailsServiceByIdOrToken implements UserDetailsServiceByIdOrToken {

    private final RedisUtils redisUtils;
    private final ObjectMapper objectMapper;

    /**
     * 根据id从redis中获取用户信息，如果不存在则认为token无效
     * 因为security-starter 中 JwtAuthenticationFilter.doFilterInternal() 在调用此方法时已经校验了token是否过期，所以这里认定token肯定没有过期
     */
    @Override
    public UserDetails loadUserByUserIdOrToken(String id, String token) {
        // 先从redis中获取用户信息
        val userEntity = redisUtils.get(RedisConstant.LOGIN_TOKEN_KEY + token, UserEntity.class);
        if (userEntity == null) {
            log.error("redis中不存在登录的用户信息:{}", id);
            throw new JwtException("redis中不存在登录的用户信息,id:" + id + "; token:" + token);
        }
        // 如果该token已不在用户活跃token集合中，则视为无效token，并且删除redis中的用户信息
        val sMembers = redisUtils.sMembers(RedisConstant.USER_TOKENS_KEY + id);
        if (sMembers == null || sMembers.isEmpty()) {
            throw new JwtException("活跃token集合为空");
        }
        if (!checkTokenIsActive(sMembers, token)) {
            throw new JwtException("token已失效");
        }
        // 角色权限(简易版本，如有需要可扩充)
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(userEntity.getRole().equals(UserRoleEnum.ADMIN) ? UserRoleEnum.ADMIN.getRole() : UserRoleEnum.USER.getRole()));
        User user = new User(userEntity.getName(), userEntity.getPassword() == null ? "" : userEntity.getPassword()
                , true, true, true
                , userEntity.getStatus().equals(UserStatusEnum.ENABLED), authorities);
        return new BlogUserDetails(userEntity, user);
    }

    /**
     * 检查sMembers中是否有该token，并且token没有过期
     *
     * @return true:token有效，false:token无效
     */
    private boolean checkTokenIsActive(Set<Object> sMembers, String token) {
        for (Object member : sMembers) {
            // 使用jackson将member反序列化为TokenExpireDTO
            val tokenExpireDTO = objectMapper.convertValue(member, TokenExpireDTO.class);
            if (token.equals(tokenExpireDTO.getToken())) {
                if (System.currentTimeMillis() < tokenExpireDTO.getExpireTimeStamp()) {
                    return true;
                }
                throw new JwtException("token已过期");
            }
        }
        return false;
    }
}
