package com.wenziyue.blog.biz.service.impl;

import com.wenziyue.security.model.LoginRequest;
import com.wenziyue.security.service.LoginService;
import com.wenziyue.security.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final JwtUtils jwtUtils;
    @Override
    public String login(LoginRequest request) {
        if ("admin".equals(request.getUsername()) && "123456".equals(request.getPassword())) {
            return this.jwtUtils.generateToken(request.getUsername());
        } else {
            throw new RuntimeException("用户名或密码错误");
        }
    }
}
