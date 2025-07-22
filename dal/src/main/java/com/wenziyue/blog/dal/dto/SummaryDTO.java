package com.wenziyue.blog.dal.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wenziyue
 */
@Data
public class SummaryDTO implements Serializable {

    private static final long serialVersionUID = 7763209587290022264L;

    private String title;

    private String content;

    private List<String> usedSlugs;

    private Long articleId;

    private String updateTime;

    public SummaryDTO(String title, String content, List<String> usedSlugs, Long articleId, String updateTime) {
        this.articleId = articleId;
        this.title = title;
        this.content = content;
        this.usedSlugs = usedSlugs;
        this.updateTime = updateTime;
    }
}
