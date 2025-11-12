package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.entity.NotifyInboxEntity;
import com.wenziyue.blog.dal.mapper.NotifyInboxMapper;
import com.wenziyue.blog.dal.service.NotifyInboxService;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Service
public class NotifyInboxServiceImpl extends ServiceImpl<NotifyInboxMapper, NotifyInboxEntity> implements NotifyInboxService {
}
