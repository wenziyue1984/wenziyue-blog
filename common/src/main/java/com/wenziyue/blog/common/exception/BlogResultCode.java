package com.wenziyue.blog.common.exception;

import com.wenziyue.framework.common.IResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author wenziyue
 */
@Getter
@RequiredArgsConstructor
public enum BlogResultCode implements IResultCode {

    REGISTER_NAME_EXIST("60010", "用户名已存在"),
    REGISTER_EMAIL_EXIST("60020", "邮箱已存在"),
    REGISTER_PHONE_EXIST("60030", "电话已存在"),
    REGISTER_NAME_OR_EMAIL_OR_PHONE_EXIST("60040", "用户名或邮箱或电话已存在"),
    REGISTER_FAIL("60050", "注册失败"),
    REGISTER_PARAM_ERROR("60060", "用户名和密码不能为空"),

    USER_NOT_EXIST("60070", "用户不存在"),
    LOGIN_PARAM_ERROR("60080", "用户名或密码错误"),
    LOGIN_PARAM_IS_EMPTY("60090", "登录参数为空"),
    LOGIN_ERROR("60092", "登录失败"),

    CAPTCHA_IS_EMPTY("60100", "验证码参数为空"),
    CAPTCHA_HAS_EXPIRED("60100", "验证码已失效"),
    CAPTCHA_IS_ERROR("60100", "验证码错误"),

    REFRESH_TOKEN_ERROR("60110", "刷新token失败"),

    UPDATE_USER_INFO_ERROR("60120", "更新用户信息失败"),

    OLD_PASSWORD_EQUAL_NEW_PASSWORD("60130", "新密码和旧密码相同"),
    ;

    private final String code;
    private final String msg;

}
