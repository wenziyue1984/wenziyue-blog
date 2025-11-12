-- 文章表增加索引，以提高feed流查询效率
CREATE INDEX IDX_TB_WZY_BLOG_ARTICLE_FEED
    ON TB_WZY_BLOG_ARTICLE (`user_id`, `deleted`, `status`, `create_time`, `id`);

-- 关注表增加索引，以提高feed流查询效率
CREATE INDEX `idx_feed`
    ON TB_WZY_BLOG_USER_FOLLOW (`user_id`, `deleted`, `follow_user_id`);
