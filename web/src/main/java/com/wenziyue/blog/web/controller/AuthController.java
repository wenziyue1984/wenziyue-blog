package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.dal.dto.LoginDTO;
import com.wenziyue.blog.biz.service.AuthService;
import com.wenziyue.blog.biz.service.BizUserService;
import com.wenziyue.blog.dal.dto.RegisterDTO;
import com.wenziyue.framework.annotation.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author wenziyue
 */
@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
@ResponseResult
@Tag(name = "权限管理", description = "用户登录、注销、刷新token相关接口")
public class AuthController {

    private final AuthService authService;
    private final BizUserService bizUserService;

    @Operation(summary = "用户注册", description = "用户注册")
    @PostMapping("/register")
    public String register(@Parameter(description = "注册参数", required = true) @RequestBody RegisterDTO dto) {
        return bizUserService.register(dto);
    }

    /**
     * 登录接口
     * @return token
     */
    @Operation(summary = "用户登录", description = "返回token")
    @PostMapping("/login")
    public String login(@Parameter(description = "登录参数", required = true) @Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }

    @Operation(summary = "google登录", description = "返回token")
    @PostMapping("/googleLogin")
    public String googleLogin(@Parameter(description = "google登录参数", required = true) @Valid @RequestBody LoginDTO dto) {
        return authService.googleLogin(dto);
    }
}
