package com.wenziyue.blog.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wenziyue.blog.common.enums.ArticleStatusEnum;
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
@TableName("TB_WZY_BLOG_ARTICLE")
public class ArticleEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("summary")
    private String summary;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("slug")
    private String slug;

    @TableField("is_top")
    private Boolean isTop;

    @TableField("sort")
    private Integer sort;

    @TableField("status")
    private ArticleStatusEnum status;
}
