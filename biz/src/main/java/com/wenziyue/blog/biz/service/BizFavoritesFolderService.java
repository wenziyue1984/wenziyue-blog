package com.wenziyue.blog.biz.service;

import com.wenziyue.blog.dal.dto.ArticleDTO;
import com.wenziyue.blog.dal.dto.FavoritesArticlePageDTO;
import com.wenziyue.blog.dal.dto.FavoritesFolderDTO;
import com.wenziyue.blog.dal.dto.FavoritesFolderPageDTO;
import com.wenziyue.blog.dal.entity.FavoritesFolderEntity;
import com.wenziyue.mybatisplus.page.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wenziyue
 */
public interface BizFavoritesFolderService {
    @Transactional
    String createFavoritesFolder(FavoritesFolderDTO dto);

    @Transactional
    String updateFavoritesFolder(FavoritesFolderDTO dto);

    @Transactional
    void deleteFavoritesFolder(String id);

    @Transactional(readOnly = true)
    PageResult<FavoritesFolderEntity> favoritesFolderPage(FavoritesFolderPageDTO dto);

    @Transactional(readOnly = true)
    PageResult<ArticleDTO> favoritesArticlePage(FavoritesArticlePageDTO dto);
}
