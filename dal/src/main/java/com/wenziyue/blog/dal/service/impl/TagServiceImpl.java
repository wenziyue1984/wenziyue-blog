package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.entity.TagEntity;
import com.wenziyue.blog.dal.mapper.TagMapper;
import com.wenziyue.blog.dal.service.TagService;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, TagEntity> implements TagService {
}
