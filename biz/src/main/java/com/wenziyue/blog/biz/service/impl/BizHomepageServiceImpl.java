package com.wenziyue.blog.biz.service.impl;

import com.wenziyue.blog.biz.security.AuthHelper;
import com.wenziyue.blog.biz.service.BizHomepageService;
import com.wenziyue.blog.dal.dto.ArticlePageDTO;
import com.wenziyue.blog.dal.dto.FeedPageDTO;
import com.wenziyue.blog.dal.service.ArticleService;
import com.wenziyue.mybatisplus.page.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizHomepageServiceImpl implements BizHomepageService {

    private final ArticleService articleService;
    private final AuthHelper authHelper;


    @Override
    public PageResult<ArticlePageDTO> pageFeed(FeedPageDTO dto) {
        return articleService.feed(dto, authHelper.getCurrentUser().getId());
    }
}
