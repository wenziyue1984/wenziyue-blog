package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.entity.UserEntity;
import com.wenziyue.blog.dal.mapper.UserMapper;
import com.wenziyue.blog.dal.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

}
