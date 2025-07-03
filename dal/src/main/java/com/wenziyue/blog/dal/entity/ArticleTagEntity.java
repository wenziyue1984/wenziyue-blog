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
@TableName("TB_WZY_BLOG_ARTICLE_TAG")
public class ArticleTagEntity extends BaseEntity {

    @TableField("article_id")
    private Long articleId;

    @TableField("tag_id")
    private Long tagId;
}
