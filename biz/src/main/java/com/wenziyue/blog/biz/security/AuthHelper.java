package com.wenziyue.blog.biz.security;

import com.wenziyue.blog.dal.entity.UserEntity;
import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author wenziyue
 */
@Component
public class AuthHelper {

    public UserEntity getCurrentUser() {
        val user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            return null;
        }
        BlogUserDetails blogUserDetails = user instanceof BlogUserDetails ? (BlogUserDetails) user : null;
        if (blogUserDetails == null) {
            return null;
        }
        return blogUserDetails.getUserEntity();
    }
}
