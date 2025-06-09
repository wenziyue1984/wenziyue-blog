package com.wenziyue.blog.biz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.wenziyue.blog.biz.service.BizUserService;
import com.wenziyue.blog.biz.utils.SecurityUtils;
import com.wenziyue.blog.common.constants.RedisConstant;
import com.wenziyue.blog.common.exception.BlogResultCode;
import com.wenziyue.blog.common.utils.BlogUtils;
import com.wenziyue.blog.dal.dto.RegisterDTO;
import com.wenziyue.blog.dal.dto.UserPageDTO;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.blog.dal.service.UserService;
import com.wenziyue.framework.exception.ApiException;
import com.wenziyue.mybatisplus.page.PageResult;
import com.wenziyue.redis.utils.RedisUtils;
import com.wenziyue.security.utils.JwtUtils;
import com.wenziyue.uid.common.Status;
import com.wenziyue.uid.core.IdGen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wenziyue
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BizUserServiceImpl implements BizUserService {

    private final UserService userService;
    private final RedisUtils redisUtils;
    private final IdGen idGen;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    @Value("${wenziyue.security.expire}")
    private long expire;

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
            val result = idGen.nextId();
            if (result.getStatus().equals(Status.EXCEPTION)) {
                log.error("获取id异常:{}", result);
                throw new ApiException(BlogResultCode.REGISTER_FAIL);
            }
            userEntity.setId(result.getId());
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

            // 生成token返回
            val token = jwtUtils.generateToken(userEntity.getId().toString());
            SecurityUtils.userInfoSaveInRedis(redisUtils, userEntity, token, expire);
            // 维护活跃token列表
            redisUtils.sAddAndExpire(RedisConstant.USER_TOKENS_KEY + userEntity.getId(), expire, TimeUnit.MILLISECONDS, token);
            return token;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注册异常", e);
            throw new ApiException(BlogResultCode.REGISTER_FAIL);
        }
    }

}
