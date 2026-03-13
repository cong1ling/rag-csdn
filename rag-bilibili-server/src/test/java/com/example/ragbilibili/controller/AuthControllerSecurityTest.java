package com.example.ragbilibili.controller;

import com.example.ragbilibili.dto.request.LoginRequest;
import com.example.ragbilibili.dto.request.RegisterRequest;
import com.example.ragbilibili.dto.response.UserResponse;
import com.example.ragbilibili.service.UserService;
import com.example.ragbilibili.util.RateLimiter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 针对安全加固改动的回归测试
 */
@WebMvcTest(AuthController.class)
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @BeforeEach
    void clearRateLimitBuckets() throws Exception {
        // 每个测试前清空限流桶，避免用例间互相影响
        clearBuckets("REGISTER_BUCKETS");
        clearBuckets("LOGIN_BUCKETS");
    }

    private void clearBuckets(String fieldName) throws Exception {
        Field field = RateLimiter.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        ((ConcurrentHashMap<?, ?>) field.get(null)).clear();
    }

    // -------------------------------------------------------------------------
    // 1. 注册限流：超过每小时 3 次后返回 RATE_LIMIT_EXCEEDED (code=1006)
    // -------------------------------------------------------------------------
    @Test
    void testRegisterRateLimit() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("user");
        when(userService.register(any())).thenReturn(response);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("validuser");
        request.setPassword("password123");
        String body = objectMapper.writeValueAsString(request);

        // 前 3 次应成功
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(jsonPath("$.code").value(200));
        }

        // 第 4 次应被限流
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1006))
                .andExpect(jsonPath("$.message").value("操作过于频繁，请稍后再试"));
    }

    // -------------------------------------------------------------------------
    // 3. 登录限流：超过每分钟 5 次后返回 RATE_LIMIT_EXCEEDED (code=1006)
    // -------------------------------------------------------------------------
    @Test
    void testLoginRateLimit() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("testuser");
        when(userService.login(any())).thenReturn(response);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        String body = objectMapper.writeValueAsString(request);

        // 前 5 次应成功
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(jsonPath("$.code").value(200));
        }

        // 第 6 次应被限流
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1006))
                .andExpect(jsonPath("$.message").value("操作过于频繁，请稍后再试"));
    }

    // -------------------------------------------------------------------------
    // 4. 用户名包含特殊字符时校验失败（@Pattern）
    // -------------------------------------------------------------------------
    @Test
    void testRegisterInvalidUsernameSpecialChars() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("bad user!");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名只能包含字母、数字和下划线"));
    }

    // -------------------------------------------------------------------------
    // 5. 用户名过短时校验失败（@Size）
    // -------------------------------------------------------------------------
    @Test
    void testRegisterUsernameTooShort() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    // -------------------------------------------------------------------------
    // 6. 登录成功后旧 session 失效，新 session 中含有 userId
    // -------------------------------------------------------------------------
    @Test
    void testLoginInvalidatesOldSession() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(42L);
        response.setUsername("testuser");
        when(userService.login(any())).thenReturn(response);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        MockHttpSession oldSession = new MockHttpSession();
        oldSession.setAttribute("someKey", "someValue");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .session(oldSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();

        // 旧 session 应已失效
        assertThat(oldSession.isInvalid()).isTrue();

        // 响应中的新 session 应含有 userId=42
        MockHttpSession newSession = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(newSession).isNotNull();
        assertThat(newSession.getAttribute("userId")).isEqualTo(42L);
    }
}
