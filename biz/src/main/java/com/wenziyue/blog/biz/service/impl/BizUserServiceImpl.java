package com.wenziyue.blog.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.security.AuthHelper;
import com.wenziyue.blog.biz.service.BizUserService;
import com.wenziyue.blog.biz.utils.IdUtils;
import com.wenziyue.blog.biz.utils.SecurityUtils;
import com.wenziyue.blog.common.enums.UserStatusEnum;
import com.wenziyue.blog.common.exception.BlogResultCode;
import com.wenziyue.blog.common.utils.BlogUtils;
import com.wenziyue.blog.dal.dto.*;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.blog.dal.service.UserService;
import com.wenziyue.framework.common.CommonCode;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.framework.utils.EnumUtils;
import com.wenziyue.idempotent.annotation.WenziyueIdempotent;
import com.wenziyue.mybatisplus.page.PageResult;
import com.wenziyue.security.utils.JwtUtils;
import com.wenziyue.uid.core.IdGen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * @author wenziyue
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BizUserServiceImpl implements BizUserService {

    private final UserService userService;
    private final IdGen idGen;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthHelper authHelper;
    private final SecurityUtils securityUtils;

    @Value("${wenziyue.security.expire}")
    private long expire;

    @Override
    public UserInfoDTO userInfo(Long id) {
        val po = userService.getById(id);
        if (po == null) {
            throw new ApiException(BlogResultCode.USER_NOT_EXIST);
        }
        return new UserInfoDTO(po);
    }

    @Override
    public PageResult<UserInfoDTO> pageUser(UserPageDTO dto) {
        return userService.page(dto, Wrappers.<UserEntity>lambdaQuery()
                        .eq(dto.getId() != null, UserEntity::getId, dto.getId())
                        .like(BlogUtils.safeTrimEmptyIsNull(dto.getName()) != null, UserEntity::getName, dto.getName())
                        .like(BlogUtils.safeTrimEmptyIsNull(dto.getEmail()) != null, UserEntity::getEmail, dto.getEmail())
                        .like(BlogUtils.safeTrimEmptyIsNull(dto.getPhone()) != null, UserEntity::getPhone, dto.getPhone())
                        .orderByDesc(UserEntity::getUpdateTime))
                .map(UserInfoDTO::new);
    }

    @Override
    public String register(RegisterDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty() || dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new ApiException(BlogResultCode.REGISTER_PARAM_ERROR);
        }
        try {
            // 判断 用户名、邮箱、手机号 是否已存在
            String email = BlogUtils.safeTrimEmptyIsNull(dto.getEmail());
            String phone = BlogUtils.safeTrimEmptyIsNull(dto.getPhone());
            String bio = BlogUtils.safeTrimEmptyIsNull(dto.getBio());
            val userEntityList = userService.list(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getName, dto.getName().trim())
                    .or(email != null, queryWrapper -> queryWrapper.eq(UserEntity::getEmail, email))
                    .or(phone != null, queryWrapper -> queryWrapper.eq(UserEntity::getPhone, phone)));
            if (!userEntityList.isEmpty()) {
                userEntityList.forEach(userEntity -> {
                    if (userEntity.getName().equals(dto.getName().trim())) {
                        throw new ApiException(BlogResultCode.REGISTER_NAME_EXIST);
                    } else if (userEntity.getEmail() != null && userEntity.getEmail().equals(email)) {
                        throw new ApiException(BlogResultCode.REGISTER_EMAIL_EXIST);
                    } else if (userEntity.getPhone() != null && userEntity.getPhone().equals(phone)) {
                        throw new ApiException(BlogResultCode.REGISTER_PHONE_EXIST);
                    }
                });
            }

            //新建用户
            UserEntity userEntity = new UserEntity();
            userEntity.setId(IdUtils.getID(idGen));
            userEntity.setName(dto.getName().trim());
            userEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
            userEntity.setEmail(email);
            userEntity.setPhone(phone);
            userEntity.setBio(bio);
            try {
                userService.save(userEntity);
            } catch (DataIntegrityViolationException e) {
                log.error("用户名或邮箱或手机已存在:{}", dto, e);
                throw new ApiException(BlogResultCode.REGISTER_NAME_OR_EMAIL_OR_PHONE_EXIST);
            }

            // 生成token返回 && 维护活跃token列表
            val token = jwtUtils.generateToken(userEntity.getId().toString());
            userEntity = userService.getById(userEntity.getId());
            securityUtils.userInfoSaveInRedisAndRefreshUserToken(userEntity, token, null, expire);
            return token;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注册异常", e);
            throw new ApiException(BlogResultCode.REGISTER_FAIL);
        }
    }

    @Override
    public UserInfoDTO userInfo() {
        val currentUser = authHelper.getCurrentUser();
        return new UserInfoDTO(currentUser);
    }

    @Override
    @WenziyueIdempotent(keys = {"#dto.id"}, prefix = "blog:updateUserInfo", timeout = 60)
    public void updateUserInfo(UserInfoDTO dto) {
        if (dto == null) {
            throw new ApiException(CommonCode.ILLEGAL_PARAMS);
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new ApiException(BlogResultCode.REGISTER_PARAM_ERROR);
        }
        val currentUser = authHelper.getCurrentUser();
        if (currentUser == null) {
            throw new ApiException(CommonCode.ACCESS_DENIED);
        }
        // 判断 用户名、邮箱、手机号 是否已存在
        String email = BlogUtils.safeTrimEmptyIsNull(dto.getEmail());
        String phone = BlogUtils.safeTrimEmptyIsNull(dto.getPhone());
        String bio = BlogUtils.safeTrimEmptyIsNull(dto.getBio());

        try {
            val userEntityList = userService.list(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getName, dto.getName().trim())
                    .or(dto.getEmail() != null, queryWrapper -> queryWrapper.eq(UserEntity::getEmail, email))
                    .or(dto.getPhone() != null, queryWrapper -> queryWrapper.eq(UserEntity::getPhone, phone)));
            if (!userEntityList.isEmpty()) {
                userEntityList.forEach(userEntity -> {
                    if (userEntity.getName().equals(dto.getName().trim()) && !currentUser.getId().equals(userEntity.getId())) {
                        throw new ApiException(BlogResultCode.REGISTER_NAME_EXIST);
                    } else if (userEntity.getEmail() != null && userEntity.getEmail().equals(email) && !currentUser.getId().equals(userEntity.getId())) {
                        throw new ApiException(BlogResultCode.REGISTER_EMAIL_EXIST);
                    } else if (userEntity.getPhone() != null && userEntity.getPhone().equals(phone) && !currentUser.getId().equals(userEntity.getId())) {
                        throw new ApiException(BlogResultCode.REGISTER_PHONE_EXIST);
                    }
                });
            }
            currentUser.setName(dto.getName().trim());
            currentUser.setPhone(phone);
            currentUser.setEmail(email);
            currentUser.setBio(bio);
            currentUser.setAvatarUrl(dto.getAvatarUrl());
            userService.updateById(currentUser);

            // 更新redis中用户信息
            securityUtils.refreshUserInfo(currentUser);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户信息失败：{}", dto, e);
            throw new ApiException(BlogResultCode.UPDATE_USER_INFO_ERROR);
        }
    }

    @Override
    public boolean checkPassword(CheckPasswordDTO dto) {
        if (dto == null) {
            throw new ApiException(CommonCode.ILLEGAL_PARAMS);
        }
        val userEntity = userService.getById(authHelper.getCurrentUser().getId());
        if (userEntity == null) {
            throw new ApiException(BlogResultCode.USER_NOT_EXIST);
        }
        return passwordEncoder.matches(dto.getPassword(), userEntity.getPassword());
    }

    @Override
    @WenziyueIdempotent(keys = {"#dto.id"}, prefix = "blog:updatePassword", timeout = 60, cleanOnFinish = false, cleanOnError = true)
    public void updatePassword(UpdatePasswordDTO dto) {
        if (dto == null) {
            throw new ApiException(CommonCode.ILLEGAL_PARAMS);
        }
        val userEntity = userService.getById(authHelper.getCurrentUser().getId());
        if (userEntity == null) {
            throw new ApiException(BlogResultCode.USER_NOT_EXIST);
        }
        if (passwordEncoder.matches(dto.getNewPassword(), userEntity.getPassword())) {
            throw new ApiException(BlogResultCode.OLD_PASSWORD_EQUAL_NEW_PASSWORD);
        }
        userEntity.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userService.updateById(userEntity);
        // 重设密码后需要重新登录，因此删除用户所有登录token
        securityUtils.clearUserAllToken(userEntity.getId());
    }

    @Override
    @WenziyueIdempotent(keys = {"#dto.id"}, prefix = "blog:changeUserStatus", timeout = 60)
    public void changeUserStatus(ChangeUserStatusDTO dto) {
        if (dto == null || dto.getId() == null || dto.getStatus() == null) {
            throw new ApiException(CommonCode.ILLEGAL_PARAMS);
        }
        // 如果dto.getStatus()对应的code不在UserStatusEnum中，那么抛出异常
        val userStatusEnum = EnumUtils.fromCode(UserStatusEnum.class, dto.getStatus());

        val userEntity = userService.getById(dto.getId());
        if (userEntity == null) {
            throw new ApiException(BlogResultCode.USER_NOT_EXIST);
        }
        if (!userEntity.getStatus().equals(userStatusEnum)) {
            userEntity.setStatus(userStatusEnum);
            userService.updateById(userEntity);
        }
        // 更新redis中用户信息
        securityUtils.refreshUserInfo(userEntity);
    }

    @Override
    @WenziyueIdempotent(keys = {"#dto.id"}, prefix = "blog:resetPassword", timeout = 60)
    public void resetPassword(UpdatePasswordDTO dto) {
        if (dto == null || dto.getNewPassword() == null || dto.getNewPassword().isEmpty()) {
            throw new ApiException(CommonCode.ILLEGAL_PARAMS);
        }
        val userEntity = userService.getById(dto.getId());
        if (userEntity == null) {
            throw new ApiException(BlogResultCode.USER_NOT_EXIST);
        }
        if (passwordEncoder.matches(dto.getNewPassword(), userEntity.getPassword())) {
            return;
        }
        userEntity.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userService.updateById(userEntity);
        // 重设密码后需要重新登录，因此删除用户所有登录token
        securityUtils.clearUserAllToken(userEntity.getId());
    }

}
