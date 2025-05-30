package com.wenziyue.blog.biz.service.impl;

import com.wenziyue.blog.biz.dto.LoginDTO;
import com.wenziyue.blog.biz.service.AuthService;
import com.wenziyue.blog.dal.service.UserService;
import com.wenziyue.security.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserService  userService;
    private final JwtUtils jwtUtils;

    @Override
    public String login(LoginDTO dto) {

        return null;
    }
}
