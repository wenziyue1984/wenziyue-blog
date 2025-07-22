package com.wenziyue.blog.common.constants;

/**
 * @author wenziyue
 */
public interface RedisConstant {

    String ID_GENERATOR_KEY = "blog:id";

    /**
     * 登录时存储用户信息
     */
    String LOGIN_TOKEN_KEY = "blog:login:userDetail:";

    /**
     * 用户所有活跃的token集合
     */
    String USER_TOKENS_KEY = "blog:login:userTokens:";

    /**
     * 验证码
     */
    String CAPTCHA_KEY = "blog:captcha:";

    /**
     * 监听生成的slug
     */
    String SLUG_LISTEN_KEY = "blog:slug:listen:";

    /**
     * 文章更新时间，防止生成summary和slug的时候文章已被修改，也可验证当前文章有无加入mq队列
     */
    String ARTICLE_UPDATE_TIME_KEY = "blog:article:updateTime:";

    /**
     * 文章点赞数量
     */
    String ARTICLE_LIKE_COUNT_KEY = "blog:article:like:count:";

    /**
     * 文章点赞用户列表
     * article:like:users:{articleId} → ZSet<userId, timestamp>
     */
    String ARTICLE_LIKE_USERS_KEY = "blog:article:like:users:";

    /**
     * 用户点赞的文章列表
     * user:like:articles:{userId} → ZSet<articleId, timestamp>
     */
    String USER_LIKE_ARTICLES_KEY = "user:like:articles:";

    /**
     * 文章点赞流
     */
    String ARTICLE_LIKE_STREAM_KEY = "blog:article:like:stream";

    /**
     * 文章点赞流组
     */
    String ARTICLE_LIKE_STREAM_GROUP_NAME = "like_group";

    /**
     * 文章点赞流消费者
     */
    String ARTICLE_LIKE_STREAM_CONSUMER_NAME = "consumer_1";
}
