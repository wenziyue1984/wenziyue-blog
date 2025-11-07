-- 聊天会话表
CREATE TABLE IF NOT EXISTS TB_WZY_BLOG_CHAT_SESSION
(
    `id`            BIGINT    NOT NULL PRIMARY KEY COMMENT '主键id',
    `version`       INT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`       TINYINT   NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time`   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `small_user_id` BIGINT    NOT NULL COMMENT '用户id较小者',
    `big_user_id`   BIGINT    NOT NULL COMMENT '用户id较大者',
    UNIQUE KEY `uk_pair` (`small_user_id`, `big_user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '聊天会话表';

-- 会话状态表
CREATE TABLE IF NOT EXISTS TB_WZY_BLOG_CHAT_SESSION_STATUS
(
    `version`          INT       NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`          TINYINT   NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time`      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `session_id`       BIGINT    NOT NULL COMMENT '聊天ID',
    `user_id`          BIGINT    NOT NULL COMMENT '用户',
    `other_user_id`    BIGINT    NOT NULL COMMENT '聊天对方ID',
    `last_read_seq`    BIGINT    NULL COMMENT '最后阅读的会话中消息序号',
    `top`              INT       NOT NULL DEFAULT 0 COMMENT '置顶 非0表示置顶，值越大顺序越高',
    `mute`             TINYINT   NOT NULL DEFAULT 0 COMMENT '免打扰',
    PRIMARY KEY (user_id, session_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '聊天记录表';

-- 聊天记录表
CREATE TABLE IF NOT EXISTS TB_WZY_BLOG_CHAT_RECORD
(
    `version`      INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`      TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time`  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `session_id`   BIGINT        NOT NULL COMMENT '聊天ID',
    `seq`          BIGINT        NOT NULL COMMENT '会话中的消息序列号',
    `chat_type`    TINYINT       NOT NULL DEFAULT 0 COMMENT '聊天类型 0-私聊(预留拓展：群聊/系统消息等)',
    `msg_type`     TINYINT       NOT NULL DEFAULT 0 COMMENT '0文本(预留扩展: 图片/文件/表情等)',
    `content`      VARCHAR(1024) NOT NULL COMMENT '聊天内容',
    `from_user_id` BIGINT        NOT NULL COMMENT '发送者ID',
    `to_user_id`   BIGINT        NOT NULL COMMENT '接收者ID',
    `status`       TINYINT       NOT NULL DEFAULT 0 COMMENT '状态 0-正常 1-撤回',
    `timestamp`    BIGINT        NOT NULL COMMENT '时间戳，当作客户端消息ID，用于幂等控制',
    PRIMARY KEY (`session_id`, `seq`),
    INDEX `idx_from_user_id` (`from_user_id`, `status`, `to_user_id`),
    UNIQUE KEY `uk_from_client` (`from_user_id`, `timestamp`) -- 幂等
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '聊天记录表';
