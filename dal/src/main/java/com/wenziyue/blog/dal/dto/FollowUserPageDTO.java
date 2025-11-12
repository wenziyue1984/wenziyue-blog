package com.wenziyue.blog.dal.dto;

import com.wenziyue.mybatisplus.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author wenziyue
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FollowUserPageDTO extends PageRequest {

    private static final long serialVersionUID = 262216769803146575L;

    @Schema(description = "排序方式，0关注时间正序，1关注时间倒序", example = "0")
    private Integer sort;

    @Schema(description = "用户名称，模糊搜索", example = "test")
    private String name;
}
