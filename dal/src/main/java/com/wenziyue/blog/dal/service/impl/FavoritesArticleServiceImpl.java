package com.wenziyue.blog.dal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wenziyue.blog.dal.dto.ArticleDTO;
import com.wenziyue.blog.dal.entity.FavoritesArticleEntity;
import com.wenziyue.blog.dal.mapper.FavoritesArticleMapper;
import com.wenziyue.blog.dal.service.FavoritesArticleService;
import com.wenziyue.mybatisplus.page.PageRequest;
import com.wenziyue.mybatisplus.page.PageResult;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * @author wenziyue
 */
@Service
public class FavoritesArticleServiceImpl extends ServiceImpl<FavoritesArticleMapper, FavoritesArticleEntity> implements FavoritesArticleService {

    @Override
    public PageResult<ArticleDTO> selectFavoritesArticlePage(PageRequest dto, Long ffId, Long userId) {
        Page<ArticleDTO> page = new Page<>(dto.getCurrent(), dto.getSize());
        val result = baseMapper.selectFavoritesArticlePage(page, ffId, userId);
        return PageResult.<ArticleDTO>builder()
                .records(result.getRecords())
                .total(result.getTotal())
                .size(result.getSize())
                .current(result.getCurrent())
                .pages(result.getPages())
                .build();
    }

    @Override
    public void cancelFavoritesArticle(Long favoritesFolderId, Long articleId, Long userId) {
        baseMapper.cancelFavoritesArticle(favoritesFolderId, articleId, userId);
    }

    @Override
    public void removeByFavoritesFolderId(Long favoritesFolderId) {
        baseMapper.removeByFavoritesFolderId(favoritesFolderId);
    }
}
