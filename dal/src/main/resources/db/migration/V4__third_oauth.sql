-- 第三方 OAuth 凭据
CREATE TABLE IF NOT EXISTS `TB_WZY_BLOG_THIRD_OAUTH`
(
    `id`          BIGINT       NOT NULL PRIMARY KEY COMMENT '主键ID',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    user_id       BIGINT       NOT NULL COMMENT '关联 TB_WZY_BLOG_USER.id',
    provider      tinyint      NOT NULL COMMENT '平台标识: google/github/gitee …',
    provider_uid  VARCHAR(128) NOT NULL COMMENT '三方用户唯一标识',
    access_token  VARCHAR(512),
    refresh_token VARCHAR(512),
    expire_time   TIMESTAMP    NULL,
    union_id      VARCHAR(128) NULL COMMENT '微信等多端合并标识',
    extra         JSON         NULL COMMENT '用 JSON 存放动态字段（头像、邮箱等冗余）',
    INDEX `IDX_TB_WZY_BLOG_THIRD_OAUTH_USER_ID` (`user_id`),
    UNIQUE INDEX `UK_TB_WZY_BLOG_THIRD_OAUTH_PROVIDER` (`provider`, `provider_uid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='第三方 OAuth 凭据';