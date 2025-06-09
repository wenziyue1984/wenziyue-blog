package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.dal.dto.LoginDTO;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wenziyue
 */
public interface AuthService {

    @Transactional(readOnly = true)
    String login(LoginDTO dto);

    String googleLogin(LoginDTO dto);

    boolean logout(String authorization);

    void forceLogout(Long string);
}
