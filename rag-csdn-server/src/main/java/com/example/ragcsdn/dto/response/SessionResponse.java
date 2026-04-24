package com.example.ragcsdn.dto.response;

import lombok.Data;

/**
 * 会话响应
 */
@Data
public class SessionResponse {
    private Long id;
    private String sessionType;
    private Long articleId;
    private String articleTitle;
    private String createTime;
    private String conversationSummary;
    private String summaryUpdateTime;
}

