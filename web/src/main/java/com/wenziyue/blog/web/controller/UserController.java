package com.wenziyue.blog.web.controller;

import com.wenziyue.blog.biz.dao.UserPageDTO;
import com.wenziyue.blog.biz.service.BizUserService;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.framework.starter.annotation.ResponseResult;
import com.wenziyue.mybatisplus.page.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author wenziyue
 */
@RestController
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
@ResponseResult
public class UserController {

    private final BizUserService bizUserService;

    @GetMapping("/{id}")
    public UserEntity testUser(@PathVariable("id") Long id) {
        return bizUserService.queryUserById(id);
    }

    @GetMapping("/page")
    public PageResult<UserEntity> listProducts(UserPageDTO dto) {
        log.info("dto:{}", dto);
        return bizUserService.pageUser(dto);
    }
}
