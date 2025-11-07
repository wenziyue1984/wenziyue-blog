package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.entity.NotifyOutboxEntity;
import com.wenziyue.blog.dal.mapper.NotifyOutboxMapper;
import com.wenziyue.blog.dal.service.NotifyOutboxService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wenziyue
 */
@Service
public class NotifyOutboxServiceImpl extends ServiceImpl<NotifyOutboxMapper, NotifyOutboxEntity> implements NotifyOutboxService {
    @Override
    public int batchSetOwner(String ownerToken, Integer batchSize) {
        return baseMapper.batchSetOwner(ownerToken, batchSize);
    }
}
