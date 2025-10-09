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
public enum ChatSessionMuteEnum implements ICommonEnum {

    NORMAL(0, "正常"),
    HIDDEN(1, "免打扰");

    @EnumValue
    private final Integer code;
    private final String desc;
}
