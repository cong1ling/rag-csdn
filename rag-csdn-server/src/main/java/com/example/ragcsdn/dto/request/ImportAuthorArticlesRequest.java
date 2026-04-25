package com.example.ragcsdn.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 批量导入作者公开文章
 */
@Data
public class ImportAuthorArticlesRequest {
    @NotBlank(message = "作者主页 URL 不能为空")
    private String authorUrl;

    private Integer maxArticles;

    private Integer maxPages;
}
