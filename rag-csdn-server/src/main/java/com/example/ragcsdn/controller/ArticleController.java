package com.example.ragcsdn.controller;

import com.example.ragcsdn.common.Result;
import com.example.ragcsdn.dto.request.ImportArticleRequest;
import com.example.ragcsdn.dto.request.ImportAuthorArticlesRequest;
import com.example.ragcsdn.dto.request.ImportRecommendedArticlesRequest;
import com.example.ragcsdn.dto.request.RebuildArticleRequest;
import com.example.ragcsdn.dto.response.ArticleResponse;
import com.example.ragcsdn.dto.response.BatchImportResponse;
import com.example.ragcsdn.service.ArticleService;
import com.example.ragcsdn.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 文章控制器
 */
@RestController
@RequestMapping({"/api/articles", "/api/videos"})
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @PostMapping
    public Result<ArticleResponse> importArticle(@Valid @RequestBody ImportArticleRequest request) {
        return Result.success(articleService.importArticle(request, UserContext.get()));
    }

    @PostMapping("/batch/author")
    public Result<BatchImportResponse> importAuthorArticles(@Valid @RequestBody ImportAuthorArticlesRequest request) {
        return Result.success(articleService.importAuthorArticles(request, UserContext.get()));
    }

    @PostMapping("/batch/recommendations")
    public Result<BatchImportResponse> importRecommendedArticles(@RequestBody(required = false) ImportRecommendedArticlesRequest request) {
        return Result.success(articleService.importRecommendedArticles(request == null ? new ImportRecommendedArticlesRequest() : request, UserContext.get()));
    }

    @PostMapping("/{id}/rebuild")
    public Result<ArticleResponse> rebuildArticle(@PathVariable Long id, @Valid @RequestBody RebuildArticleRequest request) {
        return Result.success(articleService.rebuildArticle(id, request, UserContext.get()));
    }

    @GetMapping
    public Result<List<ArticleResponse>> listArticles() {
        return Result.success(articleService.listArticles(UserContext.get()));
    }

    @GetMapping("/{id}")
    public Result<ArticleResponse> getArticle(@PathVariable Long id) {
        return Result.success(articleService.getArticle(id, UserContext.get()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id, UserContext.get());
        return Result.success();
    }
}

