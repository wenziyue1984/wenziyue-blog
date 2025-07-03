package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.entity.ArticleTagEntity;
import com.wenziyue.blog.dal.mapper.ArticleTagMapper;
import com.wenziyue.blog.dal.service.ArticleTagService;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTagEntity> implements ArticleTagService {
}
