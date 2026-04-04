package com.example.ragbilibili.controller;

import com.example.ragbilibili.dto.request.LoginRequest;
import com.example.ragbilibili.dto.request.RegisterRequest;
import com.example.ragbilibili.dto.response.UserResponse;
import com.example.ragbilibili.interceptor.LoginInterceptor;
import com.example.ragbilibili.service.UserService;
import com.example.ragbilibili.util.JwtUtil;
import com.example.ragbilibili.util.RateLimiter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

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

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private LoginInterceptor loginInterceptor;

    @BeforeEach
    void clearRateLimitBuckets() throws Exception {
        clearBuckets("REGISTER_BUCKETS");
        clearBuckets("LOGIN_BUCKETS");
    }

    private void clearBuckets(String fieldName) throws Exception {
        Field field = RateLimiter.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        ((ConcurrentHashMap<?, ?>) field.get(null)).clear();
    }

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

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(jsonPath("$.code").value(200));
        }

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(429))
                .andExpect(jsonPath("$.message").value("操作过于频繁，请稍后重试"));
    }

    @Test
    void testLoginRateLimit() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("testuser");
        when(userService.login(any())).thenReturn(response);
        when(jwtUtil.generateToken(any())).thenReturn("mocked.jwt.token");

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        String body = objectMapper.writeValueAsString(request);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(jsonPath("$.code").value(200));
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(429))
                .andExpect(jsonPath("$.message").value("操作过于频繁，请稍后重试"));
    }

    @Test
    void testRegisterInvalidUsernameSpecialChars() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("bad user!");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testRegisterUsernameTooShort() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("ab");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testLoginReturnsToken() throws Exception {
        UserResponse response = new UserResponse();
        response.setId(42L);
        response.setUsername("testuser");
        when(userService.login(any())).thenReturn(response);
        when(jwtUtil.generateToken(42L)).thenReturn("mocked.jwt.token");

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mocked.jwt.token"));
    }
}
