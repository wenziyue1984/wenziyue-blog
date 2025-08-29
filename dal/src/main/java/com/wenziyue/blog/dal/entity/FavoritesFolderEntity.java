package com.wenziyue.blog.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wenziyue.blog.common.enums.FavoritesFolderAuthEnum;
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
@TableName("TB_WZY_BLOG_FAVORITES_FOLDER")
public class FavoritesFolderEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("auth")
    private FavoritesFolderAuthEnum auth;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("user_id")
    private Long userId;
}
