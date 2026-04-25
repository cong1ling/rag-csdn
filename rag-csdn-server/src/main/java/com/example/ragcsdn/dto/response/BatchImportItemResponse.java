package com.example.ragcsdn.dto.response;

import lombok.Data;

@Data
public class BatchImportItemResponse {
    private Long articleId;
    private String sourceId;
    private String sourceUrl;
    private String title;
    private String status;
    private String message;
}
