package com.wenziyue.blog.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wenziyue.blog.dal.dto.ArticleDTO;
import com.wenziyue.blog.dal.entity.FavoritesArticleEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * @author wenziyue
 */
public interface FavoritesArticleMapper extends BaseMapper<FavoritesArticleEntity> {

    IPage<ArticleDTO> selectFavoritesArticlePage(Page<?> page, @Param("folderId") Long folderId, @Param("userId") Long userId);

    @Delete("delete from TB_WZY_FAVORITES_ARTICLE " +
            "where favorites_folder_id = #{favoritesFolderId} and article_id = #{articleId} and user_id = #{userId}")
    void cancelFavoritesArticle(@Param("favoritesFolderId") Long favoritesFolderId, @Param("articleId") Long articleId, @Param("userId") Long userId);
}
