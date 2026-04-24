package com.example.ragcsdn.controller;

import com.example.ragcsdn.dto.request.CreateSessionRequest;
import com.example.ragcsdn.dto.response.SessionResponse;
import com.example.ragcsdn.interceptor.LoginInterceptor;
import com.example.ragcsdn.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.ArgumentCaptor;

@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SessionService sessionService;

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

    @Test
    void testCreateSingleArticleSession() throws Exception {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setSessionType("SINGLE_ARTICLE");
        request.setArticleId(1L);

        SessionResponse response = new SessionResponse();
        response.setId(1L);
        response.setSessionType("SINGLE_ARTICLE");
        response.setArticleId(1L);
        response.setArticleTitle("测试文章");

        when(sessionService.createSession(any(CreateSessionRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer mocked.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sessionType").value("SINGLE_ARTICLE"))
                .andExpect(jsonPath("$.data.articleId").value(1))
                .andExpect(jsonPath("$.data.articleTitle").value("测试文章"));
    }

    @Test
    void testCreateAllArticlesSession() throws Exception {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setSessionType("ALL_ARTICLES");

        SessionResponse response = new SessionResponse();
        response.setId(2L);
        response.setSessionType("ALL_ARTICLES");

        when(sessionService.createSession(any(CreateSessionRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer mocked.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sessionType").value("ALL_ARTICLES"))
                .andExpect(jsonPath("$.data.articleId").doesNotExist());
    }

    @Test
    void testCreateSessionShouldAcceptLegacyVideoIdPayload() throws Exception {
        SessionResponse response = new SessionResponse();
        response.setId(3L);
        response.setSessionType("SINGLE_ARTICLE");
        response.setArticleId(1L);
        response.setArticleTitle("兼容文章");

        when(sessionService.createSession(any(CreateSessionRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer mocked.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionType\":\"SINGLE_VIDEO\",\"videoId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sessionType").value("SINGLE_ARTICLE"))
                .andExpect(jsonPath("$.data.articleId").value(1));

        ArgumentCaptor<CreateSessionRequest> requestCaptor = ArgumentCaptor.forClass(CreateSessionRequest.class);
        verify(sessionService, times(1)).createSession(requestCaptor.capture(), eq(1L));
        CreateSessionRequest captured = requestCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("SINGLE_VIDEO", captured.getSessionType());
        org.junit.jupiter.api.Assertions.assertEquals(1L, captured.getArticleId());
    }

    @Test
    void testListSessions() throws Exception {
        SessionResponse session1 = new SessionResponse();
        session1.setId(1L);
        session1.setSessionType("SINGLE_ARTICLE");
        session1.setArticleId(1L);
        session1.setArticleTitle("文章1");

        SessionResponse session2 = new SessionResponse();
        session2.setId(2L);
        session2.setSessionType("ALL_ARTICLES");

        List<SessionResponse> sessions = Arrays.asList(session1, session2);
        when(sessionService.listSessions(1L)).thenReturn(sessions);

        mockMvc.perform(get("/api/sessions")
                        .header("Authorization", "Bearer mocked.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].sessionType").value("SINGLE_ARTICLE"))
                .andExpect(jsonPath("$.data[1].sessionType").value("ALL_ARTICLES"));
    }

    @Test
    void testGetSession() throws Exception {
        SessionResponse response = new SessionResponse();
        response.setId(1L);
        response.setSessionType("SINGLE_ARTICLE");
        response.setArticleId(1L);
        response.setArticleTitle("测试文章");
        response.setConversationSummary("最近讨论了 Spring Boot 自动配置。");

        when(sessionService.getSession(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/sessions/1")
                        .header("Authorization", "Bearer mocked.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.sessionType").value("SINGLE_ARTICLE"))
                .andExpect(jsonPath("$.data.conversationSummary").value("最近讨论了 Spring Boot 自动配置。"));
    }

    @Test
    void testDeleteSession() throws Exception {
        doNothing().when(sessionService).deleteSession(1L, 1L);

        mockMvc.perform(delete("/api/sessions/1")
                        .header("Authorization", "Bearer mocked.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}

