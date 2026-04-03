package com.example.ragbilibili.controller;

import com.example.ragbilibili.common.Result;
import com.example.ragbilibili.dto.request.LoginRequest;
import com.example.ragbilibili.dto.request.RegisterRequest;
import com.example.ragbilibili.dto.response.UserResponse;
import com.example.ragbilibili.exception.BusinessException;
import com.example.ragbilibili.exception.ErrorCode;
import com.example.ragbilibili.service.UserService;
import com.example.ragbilibili.util.JwtUtil;
import com.example.ragbilibili.util.RateLimiter;
import com.example.ragbilibili.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${register.enabled:true}")
    private boolean registerEnabled;

    @PostMapping("/register")
    public Result<UserResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        if (!registerEnabled) {
            throw new BusinessException(ErrorCode.REGISTER_DISABLED);
        }
        String ip = getClientIp(httpRequest);
        if (!RateLimiter.allowRegister(ip)) {
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<UserResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        if (!RateLimiter.allowLogin(ip)) {
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }
        UserResponse response = userService.login(request);
        String token = jwtUtil.generateToken(response.getId());
        response.setToken(token);
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @GetMapping("/current")
    public Result<UserResponse> current() {
        Long userId = UserContext.get();
        return Result.success(userService.getCurrentUser(userId));
    }

    /**
     * 获取客户端 IP。
     * 依赖 server.forward-headers-strategy 配置：
     * - FRAMEWORK/NATIVE: Spring 自动从受信任的代理头中提取真实 IP 并填入 remoteAddr
     * - NONE: 直接使用 TCP 连接的对端 IP（适用于无反向代理直接暴露的场景）
     */
    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
