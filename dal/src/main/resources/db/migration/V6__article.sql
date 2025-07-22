-- 文章表
CREATE TABLE IF NOT EXISTS `TB_WZY_BLOG_ARTICLE`
(
    `id`          BIGINT       NOT NULL PRIMARY KEY COMMENT '主键ID',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `user_id`     BIGINT       NOT NULL COMMENT '作者ID',
    `title`       VARCHAR(128) NOT NULL COMMENT '标题',
    `content`     TEXT         NOT NULL COMMENT '内容',
    `summary`     VARCHAR(512)          DEFAULT NULL COMMENT '文章摘要',
    `cover_url`   VARCHAR(255)          DEFAULT NULL COMMENT '封面图地址',
    `view_count`  INT          NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `like_count`  INT          NOT NULL DEFAULT 0 COMMENT '点赞次数',
    `slug`        VARCHAR(128)          DEFAULT NULL COMMENT 'URL 别名，如 spring-boot-guide',
    `is_top`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶 0-否 1-是',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序值，值越大越靠前',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0-正常 1-隐藏',
    UNIQUE INDEX `UK_TB_WZY_BLOG_ARTICLE_USER_TITLE` (`user_id`, `title`),
    UNIQUE INDEX `UK_TB_WZY_BLOG_ARTICLE_USER_SLUG` (`user_id`, `slug`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文章表';

-- 文章标签表
CREATE TABLE TB_WZY_BLOG_TAG
(
    `id`          BIGINT      NOT NULL PRIMARY KEY COMMENT '主键ID',
    `version`     INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `name`        VARCHAR(64) NOT NULL COMMENT '标签名称',
    `status`      TINYINT     NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    UNIQUE INDEX `UK_TB_WZY_BLOG_TAG_NAME` (`name`),
    INDEX `IDX_TB_WZY_BLOG_TAG_STATUS_ID` (`status`, `id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '文章标签表';

-- 文章标签关系表
CREATE TABLE TB_WZY_BLOG_ARTICLE_TAG
(
    `version`     INT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`     TINYINT   NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `article_id`  BIGINT    NOT NULL COMMENT '文章ID',
    `tag_id`      BIGINT    NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`article_id`, `tag_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文章标签关系表';


-- 文章操作记录表
CREATE TABLE TB_WZY_BLOG_ARTICLE_OPERATION_LOG
(
    `id`             BIGINT      NOT NULL PRIMARY KEY COMMENT '主键ID',
    `version`        INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`        TINYINT     NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time`    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `article_id`     BIGINT      NOT NULL COMMENT '文章ID',
    `operator_id`    BIGINT      NOT NULL COMMENT '操作者ID',
    `operator_name`  VARCHAR(32) NOT NULL COMMENT '操作者name',
    `operation_type` TINYINT     NOT NULL COMMENT '操作类型：0-新增 1-修改 2-删除 3-隐藏 4-取消隐藏 5-置顶 6-取消置顶',
    `data`           text        NOT NULL COMMENT '操作数据',
    INDEX `IDX_TB_WZY_BLOG_ARTICLE_OPERATION_LOG_ARTICLE_ID` (`article_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文章操作记录表';