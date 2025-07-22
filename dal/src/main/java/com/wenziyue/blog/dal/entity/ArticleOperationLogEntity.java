package com.wenziyue.blog.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wenziyue.blog.common.enums.ArticleOperationTypeEnum;
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
@TableName("TB_WZY_BLOG_ARTICLE_OPERATION_LOG")
public class ArticleOperationLogEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("article_id")
    private Long articleId;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("operation_type")
    private ArticleOperationTypeEnum operationType;

    @TableField("data")
    private String data;
}
