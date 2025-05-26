package com.wenziyue.blog.dal.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.wenziyue.blog.common.enums.UserRoleEnum;
import com.wenziyue.blog.common.enums.UserStatusEnum;
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
@TableName("TB_WZY_BLOG_USER")
public class UserEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 用户名（唯一）
     */
    @TableField("name")
    private String name;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 密码（加密后的）
     */
    @TableField("password")
    private String password;

    /**
     * 头像 URL
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 简介
     */
    @TableField("bio")
    private String bio;

    /**
     * 用户状态
     */
    @TableField("status")
    private UserStatusEnum status;

    /**
     * 用户角色
     */
    @TableField("role")
    private UserRoleEnum role;


}
