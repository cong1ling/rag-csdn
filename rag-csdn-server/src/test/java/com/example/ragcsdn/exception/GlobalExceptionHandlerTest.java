package com.example.ragcsdn.exception;

import com.example.ragcsdn.controller.AuthController;
import com.example.ragcsdn.dto.request.RegisterRequest;
import com.example.ragcsdn.interceptor.LoginInterceptor;
import com.example.ragcsdn.service.UserService;
import com.example.ragcsdn.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证 GlobalExceptionHandler 对各类异常的处理
 */
@WebMvcTest(AuthController.class)
class GlobalExceptionHandlerTest {

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

    @Test
    void testDuplicateKeyExceptionReturnsUserAlreadyExists() throws Exception {
        when(userService.register(any())).thenThrow(
                new DuplicateKeyException("Duplicate entry 'testuser' for key 'username'"));

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    void testUnsupportedMediaTypeReturns415() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .content("{}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value(415));
    }
}

