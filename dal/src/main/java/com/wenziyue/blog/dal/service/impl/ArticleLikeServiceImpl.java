package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.entity.ArticleLikeEntity;
import com.wenziyue.blog.dal.mapper.ArticleLikeMapper;
import com.wenziyue.blog.dal.service.ArticleLikeService;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Service
public class ArticleLikeServiceImpl extends ServiceImpl<ArticleLikeMapper, ArticleLikeEntity> implements ArticleLikeService {
}
