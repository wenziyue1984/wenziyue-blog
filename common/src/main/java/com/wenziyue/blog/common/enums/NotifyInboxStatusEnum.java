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
public enum NotifyInboxStatusEnum implements ICommonEnum {

    NEW(0, "新消息"),
    READ(1, "已读");


    @EnumValue
    private final Integer code;
    private final String desc;
}
