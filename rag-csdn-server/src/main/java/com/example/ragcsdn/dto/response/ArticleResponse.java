package com.example.ragcsdn.dto.response;

import lombok.Data;

/**
 * 文章响应
 */
@Data
public class ArticleResponse {
    private Long id;
    private String sourceId;
    private String sourceUrl;
    private String title;
    private String description;
    private Integer chunkCount;
    private String importTime;
    private String status;
    private String failReason;
}

