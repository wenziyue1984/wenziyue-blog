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
public enum TagStatusEnum implements ICommonEnum {

    ENABLED(0, "正常"),
    DISABLED(1, "禁用");

    @EnumValue
    private final Integer code;
    private final String desc;
}
