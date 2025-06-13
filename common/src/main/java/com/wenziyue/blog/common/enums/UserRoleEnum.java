package com.wenziyue.blog.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.wenziyue.framework.common.ICommonEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 * 0 - 用户，1 - 管理员
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum implements ICommonEnum {
    USER(0, "用户", "USER"),
    ADMIN(1, "管理员", "ADMIN");

    @EnumValue
    private final Integer code;
    private final String desc;
    private final String role;
}
