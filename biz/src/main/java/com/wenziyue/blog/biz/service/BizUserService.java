package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.dal.dto.*;
import com.wenziyue.mybatisplus.page.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wenziyue
 */
public interface BizUserService {

    @Transactional(readOnly = true)
    UserInfoDTO userInfo(Long id);

    @Transactional(readOnly = true)
    PageResult<UserInfoDTO> pageUser(UserPageDTO dto);

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

    @Transactional
    void changeUserStatus(ChangeUserStatusDTO dto);

    @Transactional
    void resetPassword(UpdatePasswordDTO dto);

    @Transactional
    void followUser(Long userId);

    @Transactional
    void unFollowUser(Long userId);

    @Transactional(readOnly = true)
    PageResult<UserInfoDTO> pageFollowUser(FollowUserPageDTO dto);

    @Transactional(readOnly = true)
    PageResult<UserInfoDTO> pageFansUser(FansUserPageDTO dto);
}
