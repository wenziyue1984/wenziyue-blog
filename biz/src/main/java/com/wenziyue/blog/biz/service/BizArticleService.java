package com.wenziyue.blog.biz.service;


import com.wenziyue.blog.dal.dto.ArticleDTO;
import com.wenziyue.blog.dal.dto.SlugDTO;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wenziyue
 */
public interface BizArticleService {
    String generateSlug(SlugDTO dto);

    String getSlug(String listenKey);

    @Transactional
    String save(ArticleDTO dto);

    void sendSummaryMq(String title, String content, Long userId, Long articleId, Integer version);
}
