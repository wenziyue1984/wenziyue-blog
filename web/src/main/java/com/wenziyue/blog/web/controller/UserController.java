package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.dal.dto.*;
import com.wenziyue.blog.biz.service.BizUserService;
import com.wenziyue.framework.annotation.ResponseResult;
import com.wenziyue.mybatisplus.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author wenziyue
 */
@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
@ResponseResult
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    private final BizUserService bizUserService;

    // 用户前台

    @Operation(summary = "查看个人信息", description = "返回当前登录用户的昵称、头像、创建时间等")
    @GetMapping("/userInfo")
    public UserInfoDTO userInfo() {
        return bizUserService.userInfo();
    }

    /**
     * 因为登录后redis中存有用户信息，所以修改用户信息后，需要更新redis中的用户信息
     */
    @Operation(summary = "修改个人资料", description = "可更新昵称、头像、简介等字段（不改账号）")
    @PostMapping("/updateUserInfo")
    public void updateUserInfo(@Parameter(description = "修改个人资料参数", required = true) @Valid @RequestBody UserInfoDTO dto) {
        bizUserService.updateUserInfo(dto);
    }

    /**
     * 改密码时需要先验证旧密码
     */
    @Operation(summary = "校验旧密码是否正确", description = "true正确，false错误")
    @PostMapping("/checkPassword")
    public boolean checkPassword(@Parameter(description = "校验密码参数", required = true) @Valid @RequestBody CheckPasswordDTO dto) {
        return bizUserService.checkPassword(dto);
    }

    @Operation(summary = "修改密码", description = "修改密码")
    @PostMapping("/updatePassword")
    public void updatePassword(@Parameter(description = "修改密码参数", required = true) @Valid @RequestBody UpdatePasswordDTO dto) {
        bizUserService.updatePassword(dto);
    }

    // 管理后台

    @Operation(summary = "分页查询用户信息", description = "支持ID、用户名、邮箱、电话查询")
    @GetMapping("/page")
//    @PreAuthorize("hasRole('ADMIN')") // 如果使用 hasRole('ADMIN') 的话需要在security的User.authorities中的authority前加ROLE_前缀：[{"authority": "ROLE_ADMIN"}]
    @PreAuthorize("hasAuthority('ADMIN')") //security的User.authorities：[{"authority": "ADMIN"}]
    public PageResult<UserInfoDTO> pageUser(@Parameter(description = "分页请求参数", required = true) @Valid UserPageDTO dto) {
        log.info("dto:{}", dto);
        return bizUserService.pageUser(dto);
    }

    @Operation(summary = "用户详情", description = "返回用户信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public UserInfoDTO userInfo(@Parameter(description = "用户id", required = true) @PathVariable("id") Long id) {
        return bizUserService.userInfo(id);
    }

    @Operation(summary = "修改用户状态", description = "禁用/启用用户")
    @PostMapping("changeUserStatus")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void changeUserStatus(@Parameter(description = "修改用户状态参数", required = true) @Valid @RequestBody ChangeUserStatusDTO dto) {
        bizUserService.changeUserStatus(dto);
    }

    @Operation(summary = "重置密码", description = "重置密码")
    @PostMapping("resetPassword/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void resetPassword(@Parameter(description = "重置密码参数", required = true) @Valid @RequestBody UpdatePasswordDTO dto
            , @Parameter(description = "用户id", required = true) @PathVariable Long id) {
        bizUserService.resetPassword(dto, id);
    }

    //todo: 导出用户列表 可选功能，生成 Excel 下载（后续做）

}
