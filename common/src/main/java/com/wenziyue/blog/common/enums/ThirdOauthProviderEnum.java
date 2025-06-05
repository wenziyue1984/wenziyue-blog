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
public enum ThirdOauthProviderEnum implements ICommonEnum {

    GITHUB(0, "github"),
    GOOGLE(1, "google");

    @EnumValue
    private final Integer code;
    private final String desc;


}
