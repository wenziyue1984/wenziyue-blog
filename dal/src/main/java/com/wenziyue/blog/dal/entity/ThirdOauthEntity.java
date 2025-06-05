package com.wenziyue.blog.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wenziyue.blog.common.enums.ThirdOauthProviderEnum;
import com.wenziyue.mybatisplus.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author wenziyue
 */
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName(value = "TB_WZY_BLOG_THIRD_OAUTH", autoResultMap = true)
public class ThirdOauthEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /** 用户ID，关联用户表 */
    @TableField("user_id")
    private Long userId;

    /** 平台标识（如：google、github） */
    @TableField("provider")
    private ThirdOauthProviderEnum provider;

    /** 第三方平台的用户唯一ID */
    @TableField("provider_uid")
    private String providerUid;

    /** OAuth access token */
    @TableField("access_token")
    private String accessToken;

    /** OAuth refresh token */
    @TableField("refresh_token")
    private String refreshToken;

    /** 过期时间 */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 微信等多端平台合并使用的 unionId，可为空 */
    @TableField("union_id")
    private String unionId;

    /**
     * 附加字段（如头像、邮箱等冗余信息）
     * 用 JSON 存储，字段较灵活
     */
    @TableField(value = "extra", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;
}
