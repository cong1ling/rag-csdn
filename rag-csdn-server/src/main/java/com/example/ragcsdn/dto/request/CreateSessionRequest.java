package com.example.ragcsdn.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建会话请求
 */
@Data
public class CreateSessionRequest {
    @NotBlank(message = "会话类型不能为空")
    private String sessionType;

    /**
     * 文章 ID（单文章对话时必填）
     */
    @JsonAlias("videoId")
    private Long articleId;
}

