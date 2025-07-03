package com.wenziyue.blog.dal.dto;

import lombok.Data;

import java.io.Serializable;
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

    private Integer version;

    public SummaryDTO(String title, String content, List<String> usedSlugs, Long articleId, Integer version) {
        this.articleId = articleId;
        this.title = title;
        this.content = content;
        this.usedSlugs = usedSlugs;
        this.version = version;
    }
}
