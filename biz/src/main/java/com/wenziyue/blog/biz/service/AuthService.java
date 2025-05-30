package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.biz.dto.LoginDTO;

/**
 * @author wenziyue
 */
public interface AuthService {

    String login(LoginDTO dto);
}
