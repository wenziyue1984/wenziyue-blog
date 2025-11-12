package com.wenziyue.blog.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.wenziyue.framework.common.ICommonEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wenziyue
 */
@Getter
@AllArgsConstructor
public enum NotifyInboxTypeEnum implements ICommonEnum {

    COMMENT_LIKE(1, "评论点赞"),
    COMMENT_REPLY(2, "评论回复"),
    ARTICLE_LIKE(3, "文章点赞"),
    ARTICLE_COMMENT(4, "文章评论"),
    USER_FOLLOW(5, "用户关注"),
    PRIVATE_MESSAGE (6, "私信"),
    SYSTEM_NOTIFY (7, "系统通知"),
    ;

    @EnumValue
    private final Integer code;
    private final String desc;
}
