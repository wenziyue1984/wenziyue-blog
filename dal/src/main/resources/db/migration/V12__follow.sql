-- 用户关注表
CREATE TABLE IF NOT EXISTS TB_WZY_BLOG_USER_FOLLOW
(
    `id`             BIGINT    NOT NULL PRIMARY KEY COMMENT '主键id',
    `version`        INT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`        TINYINT   NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `user_id`        BIGINT    NOT NULL COMMENT '用户ID',
    `follow_user_id` BIGINT    NOT NULL COMMENT '关注人ID',
    UNIQUE KEY `uk_follow` (`user_id`, `follow_user_id`),
    INDEX `idx_follow_page` (`user_id`, `create_time`, `id`, `follow_user_id`),
    INDEX `idx_fans_page` (`follow_user_id`, `create_time`, `id`, `user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '用户关注表';