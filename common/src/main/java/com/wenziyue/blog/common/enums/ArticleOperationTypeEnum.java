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
public enum ArticleOperationTypeEnum implements ICommonEnum {

    // 0-新增 1-修改 2-删除 3-隐藏 4-取消隐藏 5-置顶 6-取消置顶
    ADD(0, "新增"),
    UPDATE(1, "修改"),
    DELETE(2, "删除"),
    HIDE(3, "隐藏"),
    CANCEL_HIDE(4, "取消隐藏"),
    SET_TOP(5, "置顶"),
    CANCEL_TOP(6, "取消置顶");

    @EnumValue
    private final Integer code;
    private final String desc;
}
