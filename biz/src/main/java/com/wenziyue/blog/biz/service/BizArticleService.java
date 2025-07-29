package com.wenziyue.blog.biz.service;


import com.wenziyue.blog.dal.dto.*;
import com.wenziyue.blog.dal.entity.TagEntity;
import com.wenziyue.mybatisplus.page.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wenziyue
 */
public interface BizArticleService {

    String generateSlug(SlugDTO dto);

    String getSlug(String listenKey);

    @Transactional
    String saveOrUpdate(ArticleDTO dto);

    void sendSummaryMq(String title, String content, Long userId, Long articleId, String updateTime);

    /**
     * 获取name对应标签dto，如果没有直接新建
     */
    @Transactional
    TagDTO getTag(TagDTO dto);

    @Transactional
    void changeTagStatus(TagDTO dto);

    @Transactional(readOnly = true)
    ArticleDTO getArticleDetail(Long id);

    @Transactional(readOnly = true)
    PageResult<ArticleDTO> pageArticle(ArticlePageDTO dto);

    @Transactional(readOnly = true)
    PageResult<TagEntity> pageArticleTag(TagPageDTO dto);

    @Transactional
    void hideArticle(Long id);

    @Transactional
    void deleteArticle(Long id);

    @Transactional
    void setTopArticle(Long id);

    @Transactional
    void cancelTopArticle(Long id);

    void likeArticle(Long articleId);

    void cancelLikeArticle(Long articleId);

    void pv(ArticlePvDTO dto);

    @Transactional
    void favoritesArticle(FavoritesArticleDTO dto);

    @Transactional
    void cancelFavoritesArticle(FavoritesArticleDTO dto);
}
