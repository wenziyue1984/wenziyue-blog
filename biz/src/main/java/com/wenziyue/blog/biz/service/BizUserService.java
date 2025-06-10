package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.dal.dto.*;
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

    @Transactional
    String register(RegisterDTO dto);

    @Transactional(readOnly = true)
    UserInfoDTO userInfo();

    @Transactional
    void updateUserInfo(UserInfoDTO dto);

    @Transactional(readOnly = true)
    boolean checkPassword(CheckPasswordDTO dto);

    @Transactional
    void updatePassword(UpdatePasswordDTO dto);
}
