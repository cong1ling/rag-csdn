package com.example.ragcsdn.service;

import com.example.ragcsdn.dto.request.LoginRequest;
import com.example.ragcsdn.dto.request.RegisterRequest;
import com.example.ragcsdn.dto.request.UpdateCsdnSessionRequest;
import com.example.ragcsdn.dto.response.UserResponse;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 用户注册
     */
    UserResponse register(RegisterRequest request);

    /**
     * 用户登录
     */
    UserResponse login(LoginRequest request);

    /**
     * 用户登出
     */
    void logout(Long userId);

    /**
     * 获取当前用户信息
     */
    UserResponse getCurrentUser(Long userId);

    /**
     * 保存用户 CSDN 登录态
     */
    UserResponse saveCsdnSession(UpdateCsdnSessionRequest request, Long userId);

    /**
     * 清除用户 CSDN 登录态
     */
    UserResponse clearCsdnSession(Long userId);

    /**
     * 获取用户 CSDN 登录态 Cookie
     */
    String getCsdnSessionCookie(Long userId);
}

