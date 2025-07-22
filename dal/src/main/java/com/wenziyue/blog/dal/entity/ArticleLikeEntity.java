package com.wenziyue.blog.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.wenziyue.blog.common.enums.ArticleLikeTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author wenziyue
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("TB_WZY_BLOG_ARTICLE_LIKE")
public class ArticleLikeEntity implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("article_id")
    private Long articleId;

    @TableField("user_id")
    private Long userId;

    @TableField( "time")
    private Long time;

    @TableField("type")
    private ArticleLikeTypeEnum type;
}
