package com.wenziyue.blog.biz.security;

import com.wenziyue.blog.dal.entity.UserEntity;
import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;


/**
 * 自定义UserDetails，携带UserEntity信息
 *
 * @author wenziyue
 */
@Getter
public class BlogUserDetails extends User {

    private final UserEntity userEntity;

    public BlogUserDetails(UserEntity userEntity, User user) {
        super(user.getUsername(), user.getPassword(), user.isEnabled(), user.isAccountNonExpired(), user.isCredentialsNonExpired(), user.isAccountNonLocked(), user.getAuthorities());
        this.userEntity = userEntity;
    }
}
