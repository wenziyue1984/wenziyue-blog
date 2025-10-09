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
public enum ChatTypeEnum implements ICommonEnum {

    SINGLE(0, "单聊"),
    GROUP(1, "群聊");

    @EnumValue
    private final Integer code;
    private final String desc;
}
