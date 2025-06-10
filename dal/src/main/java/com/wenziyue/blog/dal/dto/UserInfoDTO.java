package com.wenziyue.blog.dal.dto;

import com.wenziyue.blog.common.enums.UserRoleEnum;
import com.wenziyue.blog.common.enums.UserStatusEnum;
import com.wenziyue.blog.dal.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
public class UserInfoDTO implements Serializable {

    private static final long serialVersionUID = -4295527761015760446L;

    @Schema(description = "id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "用户名（唯一）")
    @NotBlank
    private String name;

    @Schema(description = "头像 URL", requiredMode = Schema.RequiredMode.AUTO)
    private String avatarUrl;

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.AUTO)
    private String email;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.AUTO)
    private String phone;

    @Schema(description = "简介", requiredMode = Schema.RequiredMode.AUTO)
    private String bio;

    @Schema(description = "用户状态", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UserStatusEnum status;

    @Schema(description = "用户角色", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private UserRoleEnum role;

    public UserInfoDTO(UserEntity userEntity) {
        if (userEntity == null) {
            return;
        }
        this.id = userEntity.getId();
        this.name = userEntity.getName();
        this.avatarUrl = userEntity.getAvatarUrl();
        this.email = userEntity.getEmail();
        this.phone = userEntity.getPhone();
        this.bio = userEntity.getBio();
        this.status = userEntity.getStatus();
        this.role = userEntity.getRole();
    }
}
