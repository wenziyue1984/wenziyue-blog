-- 本地消息表
CREATE TABLE IF NOT EXISTS TB_WZY_BLOG_NOTIFY_OUTBOX
(
    `id`                BIGINT       NOT NULL PRIMARY KEY COMMENT '主键id',
    `version`           INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `deleted`           TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除 0-正常 1-删除',
    `create_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `comment_id`        BIGINT       NOT NULL COMMENT '评论ID',
    `user_id`           BIGINT       NOT NULL COMMENT '点赞者ID',
    `recipient_user_id` BIGINT       NOT NULL COMMENT '接收者ID',
    `status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0 NEW 未发送/ 1 SENDING 发送中/ 2 SENT 发送成功/ 3 FAILED 发送失败',
    `owner`             VARCHAR(256) NULL COMMENT '本次派发者实例标识',
    `claim_time`        TIMESTAMP    NULL COMMENT '抢占时间',
    INDEX `idx_claim_pick` (status, id),
    INDEX `idx_owner_status_id` (owner, status, id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '本地消息表';