package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.dto.FansUserPageDTO;
import com.wenziyue.blog.dal.dto.FollowUserPageDTO;
import com.wenziyue.blog.dal.dto.UserInfoDTO;
import com.wenziyue.blog.dal.entity.UserFollowEntity;
import com.wenziyue.blog.dal.mapper.UserFollowMapper;
import com.wenziyue.blog.dal.service.UserFollowService;
import com.wenziyue.mybatisplus.page.PageResult;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollowEntity> implements UserFollowService {
    @Override
    public void physicalDelete(Long currentUserId, Long userId) {
        baseMapper.physicalDelete(currentUserId, userId);
    }

    @Override
    public PageResult<UserInfoDTO> pageFollowUser(FollowUserPageDTO dto, Long userId) {
        Page<UserInfoDTO> page = new Page<>(dto.getCurrent(), dto.getSize());
        val result = baseMapper.pageFollowUser(page, dto.getSort(), dto.getName(), userId);
        return PageResult.<UserInfoDTO>builder()
                .records(result.getRecords())
                .total(result.getTotal())
                .size(result.getSize())
                .current(result.getCurrent())
                .pages(result.getPages())
                .build();
    }

    @Override
    public PageResult<UserInfoDTO> pageFansUser(FansUserPageDTO dto, Long userId) {
        Page<UserInfoDTO> page = new Page<>(dto.getCurrent(), dto.getSize());
        val result = baseMapper.pageFansUser(page, dto.getSort(), dto.getName(), userId);
        return PageResult.<UserInfoDTO>builder()
                .records(result.getRecords())
                .total(result.getTotal())
                .size(result.getSize())
                .current(result.getCurrent())
                .pages(result.getPages())
                .build();
    }
}
