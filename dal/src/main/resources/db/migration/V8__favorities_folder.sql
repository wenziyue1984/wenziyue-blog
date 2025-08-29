-- 收藏夹表
CREATE TABLE IF NOT EXISTS TB_WZY_BLOG_FAVORITES_FOLDER
(
    `id`          BIGINT      NOT NULL PRIMARY KEY COMMENT '主键ID',
    `version`     INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `name`        VARCHAR(20) NOT NULL COMMENT '收藏夹名称',
    `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
    `auth`        TINYINT     NOT NULL DEFAULT 0 COMMENT '权限：0公开，1隐藏',
    `cover_url`   VARCHAR(255)         DEFAULT NULL COMMENT '封面图地址',
    UNIQUE INDEX `UK_TB_WZY_FAVORITES_FOLDER_USER_ID_NAME` (`user_id`, `name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '收藏夹表';

-- 文章收藏表
CREATE TABLE IF NOT EXISTS TB_WZY_BLOG_FAVORITES_ARTICLE
(
    `version`             INT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`             TINYINT   NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time`         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `article_id`          BIGINT    NOT NULL COMMENT '文章ID',
    `favorites_folder_id` BIGINT    NOT NULL COMMENT '收藏夹ID',
    `user_id`             BIGINT    NOT NULL COMMENT '用户ID',
    PRIMARY KEY (`favorites_folder_id`, `article_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '文章收藏表';