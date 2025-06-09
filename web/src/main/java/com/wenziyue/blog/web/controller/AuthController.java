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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    @Value("${wenziyue.security.token-header}")
    private String tokenHeader;

    /**
     * 注册成功返回token，不用再次登录
     */
    @Operation(summary = "用户注册", description = "注册成功返回token")
    @PostMapping("/register")
    public String register(@Parameter(description = "注册参数", required = true) @RequestBody RegisterDTO dto) {
        return bizUserService.register(dto);
    }

    /**
     * 登录流程：校验验证码 -> 校验用户名密码 -> 生成token -> 保存用户信息到redis -> 维护redis中用户活跃token集合 -> 返回token
     * 其中保存用户信息到redis时，key为token，过期时间与token过期时间一致，将此缓存作为校验token的依据，这样用户在不同客户端登录时互不影响。
     * 如果redis中没有此token的缓存，就算token解析有效也认为此token已失效。
     */
    @Operation(summary = "用户登录", description = "返回token")
    @PostMapping("/login")
    public String login(@Parameter(description = "登录参数", required = true) @Valid @RequestBody LoginDTO dto) {
        return authService.login(dto);
    }

    /**
     * 退出登录时只要删除redis中的token缓存，然后再维护一下用户的活跃token即可
     */
    @Operation(summary = "退出登录", description = "注销token")
    @GetMapping("/logout")
    public boolean logout(HttpServletRequest request) {
        return authService.logout(request.getHeader(tokenHeader));
    }

    /**
     * 清空redis中用户的活跃token集合
     */
    @Operation(summary = "强制用户下线", description = "强制用户下线")
    @GetMapping("/forceLogout/{id}")
    public void forceLogout(@Parameter(description = "用户id", required = true) @PathVariable("id") Long id) {
        authService.forceLogout(id);
    }


    @Operation(summary = "google登录", description = "返回token")
    @PostMapping("/googleLogin")
    public String googleLogin(@Parameter(description = "google登录参数", required = true) @Valid @RequestBody LoginDTO dto) {
        return authService.googleLogin(dto);
    }



}
