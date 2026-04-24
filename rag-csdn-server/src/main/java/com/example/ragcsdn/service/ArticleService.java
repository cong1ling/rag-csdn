package com.example.ragcsdn.service;

import com.example.ragcsdn.dto.request.ImportArticleRequest;
import com.example.ragcsdn.dto.request.RebuildArticleRequest;
import com.example.ragcsdn.dto.response.ArticleResponse;
import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService {
    /**
     * 导入文章
     */
    ArticleResponse importArticle(ImportArticleRequest request, Long userId);

    /**
     * 重建已有文章的切分与向量索引
     */
    ArticleResponse rebuildArticle(Long articleId, RebuildArticleRequest request, Long userId);

    /**
     * 获取用户文章列表
     */
    List<ArticleResponse> listArticles(Long userId);

    /**
     * 获取文章详情
     */
    ArticleResponse getArticle(Long articleId, Long userId);

    /**
     * 删除文章（级联删除分片、向量、会话）
     */
    void deleteArticle(Long articleId, Long userId);
}

