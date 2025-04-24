package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.biz.dao.UserPageDTO;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.mybatisplus.page.PageResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wenziyue
 */
public interface BizUserService {

    @Transactional(readOnly = true)
    List<UserEntity> queryUserList();

    @Transactional(readOnly = true)
    UserEntity queryUserById(Long id);

    @Transactional(readOnly = true)
    PageResult<UserEntity> pageUser(UserPageDTO dto);
}
