package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.entity.ArticleEntity;
import com.wenziyue.blog.dal.mapper.ArticleMapper;
import com.wenziyue.blog.dal.service.ArticleService;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, ArticleEntity> implements ArticleService {
}