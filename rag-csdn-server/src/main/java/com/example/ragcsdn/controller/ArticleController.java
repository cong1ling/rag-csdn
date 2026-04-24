package com.example.ragcsdn.controller;

import com.example.ragcsdn.common.Result;
import com.example.ragcsdn.dto.request.ImportArticleRequest;
import com.example.ragcsdn.dto.request.RebuildArticleRequest;
import com.example.ragcsdn.dto.response.ArticleResponse;
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

