package com.wenziyue.blog.dal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wenziyue.mybatisplus.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author wenziyue
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("TB_WZY_FAVORITES_ARTICLE")
public class FavoritesArticleEntity extends BaseEntity {

    @TableField("article_id")
    private Long articleId;

    @TableField("favorites_folder_id")
    private Long favoritesFolderId;

    @TableField("user_id")
    private Long userId;
}
