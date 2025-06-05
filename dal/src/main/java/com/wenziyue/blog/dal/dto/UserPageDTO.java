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
public class UserPageDTO extends PageRequest {

    private static final long serialVersionUID = 4009990582488251538L;

    @Schema(description = "用户id", example = "1")
    private Long id;

    @Schema(description = "用户名称", example = "wenziyue")
    private String name;

    @Schema(description = "邮箱", example = "example@xxx.com")
    private String email;

    @Override
    public String toString() {
        return "UserPageDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", size='" + getSize() + '\'' +
                ", current='" + getCurrent() + '\'' +
                '}';
    }
}
