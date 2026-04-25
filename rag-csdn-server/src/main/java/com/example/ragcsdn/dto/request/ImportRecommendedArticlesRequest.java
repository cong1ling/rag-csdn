package com.example.ragcsdn.dto.request;

import lombok.Data;

/**
 * 批量导入首页公开推荐文章
 */
@Data
public class ImportRecommendedArticlesRequest {
    private Integer limit;
}
