-- 用户表
drop table if exists TB_WZY_BLOG_USER;
CREATE TABLE `TB_WZY_BLOG_USER`
(
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `name`        VARCHAR(64)  NOT NULL COMMENT '用户名',
    `nickname`    VARCHAR(64)           DEFAULT NULL COMMENT '昵称',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码（加密后的）',
    `avatar_url`  VARCHAR(512)          DEFAULT NULL COMMENT '头像URL',
    `email`       VARCHAR(128)          DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `bio`         VARCHAR(500) NULL COMMENT '简介',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0-正常 1-禁用',
    `role`        TINYINT      NOT NULL DEFAULT 0 COMMENT '角色 0-用户, 1-管理员）',
    INDEX `IDX_TB_WZY_BLOG_USER_NAME` (`name`),
    INDEX `IDX_TB_WZY_BLOG_USER_PHONE` (`phone`),
    INDEX `IDX_TB_WZY_BLOG_USER_EMAIL` (`email`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='用户表';


INSERT INTO TB_WZY_BLOG_USER (name, nickname, password, avatar_url, email, phone, bio, status, role)
VALUES ('wenziyue', 'wzy', '12345678', 'https://example.com/avatar.jpg', 'wenziyue@example.com', '12345678901',
        'A blog enthusiast', 0, 1);


