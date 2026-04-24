package com.example.ragcsdn.controller;

import com.example.ragcsdn.dto.request.ImportArticleRequest;
import com.example.ragcsdn.dto.request.RebuildArticleRequest;
import com.example.ragcsdn.dto.response.ArticleResponse;
import com.example.ragcsdn.interceptor.LoginInterceptor;
import com.example.ragcsdn.service.ArticleService;
import com.example.ragcsdn.util.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private LoginInterceptor loginInterceptor;

    @BeforeEach
    void mockAuth() throws Exception {
        UserContext.set(1L);
        when(loginInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @AfterEach
    void clearAuth() {
        UserContext.remove();
    }

    private String buildImportJson(String articleUrl) {
        return String.format(
                "{\"articleUrl\":\"%s\"}",
                articleUrl);
    }

    private String buildRebuildJson() {
        return "{}";
    }

    @Test
    void testImportArticle() throws Exception {
        ArticleResponse response = new ArticleResponse();
        response.setId(1L);
        response.setSourceId("147000001");
        response.setSourceUrl("https://blog.csdn.net/test_author/article/details/147000001");
        response.setTitle("导入中...");
        response.setStatus("IMPORTING");

        when(articleService.importArticle(any(ImportArticleRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/articles")
                        .header("Authorization", "Bearer mocked.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildImportJson("https://blog.csdn.net/test_author/article/details/147000001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sourceId").value("147000001"))
                .andExpect(jsonPath("$.data.status").value("IMPORTING"));
    }

    @Test
    void testListArticles() throws Exception {
        ArticleResponse article1 = new ArticleResponse();
        article1.setId(1L);
        article1.setSourceId("147000001");
        article1.setTitle("文章1");
        article1.setStatus("SUCCESS");

        ArticleResponse article2 = new ArticleResponse();
        article2.setId(2L);
        article2.setSourceId("147000002");
        article2.setTitle("文章2");
        article2.setStatus("SUCCESS");

        List<ArticleResponse> articles = Arrays.asList(article1, article2);
        when(articleService.listArticles(1L)).thenReturn(articles);

        mockMvc.perform(get("/api/articles")
                        .header("Authorization", "Bearer mocked.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].sourceId").value("147000001"))
                .andExpect(jsonPath("$.data[1].sourceId").value("147000002"));
    }

    @Test
    void testRebuildArticle() throws Exception {
        ArticleResponse response = new ArticleResponse();
        response.setId(1L);
        response.setSourceId("147000001");
        response.setTitle("测试文章");
        response.setStatus("IMPORTING");

        when(articleService.rebuildArticle(eq(1L), any(RebuildArticleRequest.class), eq(1L)))
                .thenReturn(response);

        mockMvc.perform(post("/api/articles/1/rebuild")
                        .header("Authorization", "Bearer mocked.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildRebuildJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("IMPORTING"));
    }

    @Test
    void testGetArticle() throws Exception {
        ArticleResponse response = new ArticleResponse();
        response.setId(1L);
        response.setSourceId("147000001");
        response.setTitle("测试文章");
        response.setStatus("SUCCESS");

        when(articleService.getArticle(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/articles/1")
                        .header("Authorization", "Bearer mocked.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.sourceId").value("147000001"));
    }

    @Test
    void testDeleteArticle() throws Exception {
        doNothing().when(articleService).deleteArticle(1L, 1L);

        mockMvc.perform(delete("/api/articles/1")
                        .header("Authorization", "Bearer mocked.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

