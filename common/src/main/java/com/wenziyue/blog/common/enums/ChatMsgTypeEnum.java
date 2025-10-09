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
public enum ChatMsgTypeEnum implements ICommonEnum {

    TEXT(0, "文本"),
    IMAGE(1, "图片"),
    VOICE(2, "语音"),
    VIDEO(3, "视频"),
    FILE(4, "文件");

    @EnumValue
    private final Integer code;
    private final String desc;

}
