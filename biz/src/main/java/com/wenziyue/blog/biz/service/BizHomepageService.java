package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.dal.dto.ArticlePageDTO;
import com.wenziyue.blog.dal.dto.FeedPageDTO;
import com.wenziyue.mybatisplus.page.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wenziyue
 */
public interface BizHomepageService {

    @Transactional(readOnly = true)
    PageResult<ArticlePageDTO> pageFeed(FeedPageDTO dto);

}
