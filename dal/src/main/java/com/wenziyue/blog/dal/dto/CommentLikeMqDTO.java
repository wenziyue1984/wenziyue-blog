package com.wenziyue.blog.dal.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @author wenziyue
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeMqDTO implements Serializable {

    private static final long serialVersionUID = -4899807853924021672L;

    private Long commentId;

    private Long userId;

    /**
     * 0点赞 1取消点赞
     */
    private Integer type;
}
