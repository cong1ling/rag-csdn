package com.example.ragcsdn.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 导入文章请求
 */
@Data
public class ImportArticleRequest {
    @NotBlank(message = "CSDN文章URL不能为空")
    @JsonAlias("bvidOrUrl")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String articleUrl;

    @Override
    public String toString() {
        return "ImportArticleRequest{" +
                "articleUrl='" + articleUrl + '\'' +
                '}';
    }
}

