package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.biz.dto.LoginDTO;
import com.wenziyue.blog.biz.service.AuthService;
import com.wenziyue.framework.annotation.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 登录接口
     * @return token
     */
    @Operation(summary = "用户登录", description = "返回token")
    @PostMapping("/login")
    public String login(@Parameter(description = "登录参数", required = true) @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }
}
