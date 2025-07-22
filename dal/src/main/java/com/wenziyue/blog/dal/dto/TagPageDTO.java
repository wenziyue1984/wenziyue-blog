package com.wenziyue.blog.dal.dto;

import com.wenziyue.mybatisplus.page.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;

/**
 * @author wenziyue
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TagPageDTO extends PageRequest {

    private static final long serialVersionUID = 545048991775552451L;

    @Schema(description = "标签id", example = "1")
    private Long id;

    @Schema(description = "标签名称", example = "tag")
    @Size(max = 10, message = "标签长度应小于10个字符")
    private String name;

    @Schema(description = "标签状态 0正常 1禁用", example = "1")
    private Integer status;
}
