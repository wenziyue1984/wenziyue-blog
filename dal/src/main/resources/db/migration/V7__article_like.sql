-- 点赞行为表
CREATE TABLE TB_WZY_BLOG_ARTICLE_LIKE
(
    `id`         BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY COMMENT '主键ID',
    `article_id` BIGINT                NOT NULL COMMENT '文章ID',
    `user_id`    BIGINT                NOT NULL COMMENT '用户ID',
    `type`       TINYINT               NOT NULL COMMENT '点赞类型：0-点赞 1-取消点赞',
    `time`       BIGINT                NOT NULL COMMENT '发生时间，System.currentTimeMillis()'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='点赞行为表';

-- 文章表添加索引
ALTER TABLE TB_WZY_BLOG_ARTICLE
    ADD INDEX `IDX_TB_WZY_BLOG_ARTICLE_ID_LIKE` (`id`, `like_count`);

