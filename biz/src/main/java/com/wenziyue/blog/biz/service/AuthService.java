package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.dal.dto.LoginDTO;

/**
 * @author wenziyue
 */
public interface AuthService {

    String login(LoginDTO dto);

    String googleLogin(LoginDTO dto);
}
