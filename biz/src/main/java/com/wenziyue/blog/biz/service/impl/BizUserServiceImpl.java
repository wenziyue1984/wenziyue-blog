package com.wenziyue.blog.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.dao.UserPageDTO;
import com.wenziyue.blog.biz.service.BizUserService;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.blog.dal.service.UserService;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.mybatisplus.page.PageResult;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.uid.utils.UidUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wenziyue
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BizUserServiceImpl implements BizUserService {

    private final UserService userService;
    private final RedisUtils redisUtils;
    private final UidUtils uidUtils;

    @Override
    public List<UserEntity> queryUserList() {
        return userService.list();
    }

    @Override
    public UserEntity queryUserById(Long id) {
        val po = userService.getById(id);
        if (po == null) {
            throw new ApiException("800", "无此用户");
        }
        return po;
    }

    @Override
    public PageResult<UserEntity> pageUser(UserPageDTO dto) {
        return userService.page(dto, Wrappers.<UserEntity>lambdaQuery()
                .eq(dto.getId() != null, UserEntity::getId, dto.getId())
                .like(dto.getName() != null, UserEntity::getName, dto.getName())
                .like(dto.getEmail() != null, UserEntity::getEmail, dto.getEmail())
                .orderByDesc(UserEntity::getUpdateTime));
    }

    @Override
    public Long testUid() {
        val id = uidUtils.nextId();
        log.info("id:{}", id);
        return id;
    }
}
