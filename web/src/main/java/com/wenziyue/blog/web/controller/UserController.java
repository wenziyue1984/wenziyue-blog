package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.dal.dto.UserPageDTO;
import com.wenziyue.blog.biz.service.BizUserService;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.framework.annotation.ResponseResult;
import com.wenziyue.mybatisplus.page.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "用户详情", description = "返回用户信息")
    @GetMapping("/{id}")
    public UserEntity testUser(@Parameter(description = "用户id", required = true) @PathVariable("id") Long id) {
        return bizUserService.queryUserById(id);
    }

    @Operation(summary = "分页查询用户信息", description = "分页结果")
    @GetMapping("/page")
//    @PreAuthorize("hasRole('ADMIN')") // 如果使用 hasRole('ADMIN') 的话需要在security的User.authorities中的authority前加ROLE_前缀：[{"authority": "ROLE_ADMIN"}]
    @PreAuthorize("hasAuthority('ADMIN')") //security的User.authorities：[{"authority": "ADMIN"}]
    public PageResult<UserEntity> listProducts(@Parameter(description = "分页请求参数", required = true) UserPageDTO dto) {
        log.info("dto:{}", dto);
        return bizUserService.pageUser(dto);
    }


    //todo 用户模块

    //用户注册 支持邮箱 / 手机号 + 密码注册（第一期只支持账号密码）

    //用户登录 基于用户名 + 密码，登录成功发放 Token（JWT）

    //用户退出 清除本地 Token 或 Redis 中记录（视登录方案而定）

    //查看个人信息 返回当前登录用户的昵称、头像、创建时间等

    //修改个人资料 可更新昵称、头像、简介等字段（不改账号）

    //修改密码 提供旧密码验证后更改新密码功能

    //查询作者主页 通过 userId 查询某个作者的公开信息及文章列表

    //校验用户名是否重复 注册页实时校验接口，前端常见需求

    //是否已登录 前端调用时判断是否为登录状态（可返回简要信息）

    // TODO 管理后台

    //用户分页查询 支持关键词搜索、状态筛选、分页排序

    //查看用户详情 查看某个用户的全部资料（含注册时间、最近活跃、文章数等）

    //禁用/启用用户 修改状态字段（如 status=0 禁用，1 启用）

    //逻辑删除用户 不物理删除，仅做状态标记

    //重置密码 设置新密码（管理员重置）

    //后台添加用户 管理员可新建用户账号

    //导出用户列表 可选功能，生成 Excel 下载（后续做）

}
