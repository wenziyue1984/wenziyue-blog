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
public enum ChatRecordStatusEnum implements ICommonEnum {

    NORMAL(0, "正常"),
    REVOKED(1, "已撤回");

    @EnumValue
    private final Integer code;
    private final String desc;
}
