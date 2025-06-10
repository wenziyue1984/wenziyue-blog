package com.wenziyue.blog.dal.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
public class CheckPasswordDTO implements Serializable {

    private static final long serialVersionUID = 65569363508558167L;

    @NotBlank
    private String password;
}
