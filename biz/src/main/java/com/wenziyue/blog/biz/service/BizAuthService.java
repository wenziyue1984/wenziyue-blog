package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.dal.dto.GoogleLoginDTO;
import com.wenziyue.blog.dal.dto.LoginDTO;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wenziyue
 */
public interface BizAuthService {

    @Transactional(readOnly = true)
    String login(LoginDTO dto);

    @Transactional
    String googleLogin(GoogleLoginDTO dto);

    boolean logout(String authorization);

    void forceLogout(Long string);
}
