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
public enum FavoritesFolderAuthEnum implements ICommonEnum {

    ENABLED(0, "公开"),
    DISABLED(1, "隐藏");

    @EnumValue
    private final Integer code;
    private final String desc;
}
