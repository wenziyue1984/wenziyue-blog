package com.wenziyue.blog.dal.service;

import com.wenziyue.blog.dal.dto.ArticleDTO;
import com.wenziyue.blog.dal.dto.ArticlePageDTO;
import com.wenziyue.blog.dal.dto.FeedPageDTO;
import com.wenziyue.blog.dal.entity.ArticleEntity;
import com.wenziyue.mybatisplus.base.PageExtendService;
import com.wenziyue.mybatisplus.page.PageResult;

/**
 * @author wenziyue
 */
public interface ArticleService extends PageExtendService<ArticleEntity> {

    PageResult<ArticleDTO> page(ArticlePageDTO dto);

    PageResult<ArticlePageDTO> feed(FeedPageDTO dto, Long id);
}
