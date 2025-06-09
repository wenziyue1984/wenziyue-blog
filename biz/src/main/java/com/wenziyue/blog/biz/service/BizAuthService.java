package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.dal.dto.GoogleLoginDTO;
import com.wenziyue.blog.dal.dto.LoginDTO;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wenziyue
 */
public interface BizAuthService {

    @Transactional(readOnly = true)
    String login(LoginDTO dto);

    @Transactional
    String googleLogin(GoogleLoginDTO dto);

    boolean logout(HttpServletRequest request);

    void forceLogout(Long string);

    @Transactional(readOnly = true)
    boolean nameExists(String name);
}
