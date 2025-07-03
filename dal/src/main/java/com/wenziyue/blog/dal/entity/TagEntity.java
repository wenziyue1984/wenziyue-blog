package com.wenziyue.blog.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wenziyue.blog.common.enums.TagStatusEnum;
import com.wenziyue.mybatisplus.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 文章标签
 *
 * @author wenziyue
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("TB_WZY_BLOG_TAG")
public class TagEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("status")
    private TagStatusEnum status;
}
