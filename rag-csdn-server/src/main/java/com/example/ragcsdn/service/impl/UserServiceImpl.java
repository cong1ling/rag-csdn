package com.example.ragcsdn.service.impl;

import com.example.ragcsdn.dto.request.LoginRequest;
import com.example.ragcsdn.dto.request.RegisterRequest;
import com.example.ragcsdn.dto.request.UpdateCsdnSessionRequest;
import com.example.ragcsdn.dto.response.UserResponse;
import com.example.ragcsdn.entity.User;
import com.example.ragcsdn.exception.BusinessException;
import com.example.ragcsdn.exception.ErrorCode;
import com.example.ragcsdn.mapper.UserMapper;
import com.example.ragcsdn.service.UserService;
import com.example.ragcsdn.util.CredentialCryptoService;
import com.example.ragcsdn.util.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CredentialCryptoService credentialCryptoService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public UserResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordEncoder.encode(request.getPassword()));
        user.setCreateTime(LocalDateTime.now());

        // 插入数据库
        userMapper.insert(user);

        return convertToResponse(user);
    }

    @Override
    public UserResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!PasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        return convertToResponse(user);
    }

    @Override
    public void logout(Long userId) {
        // Session 认证方式，登出由 Controller 处理 session.invalidate()
        // 这里不需要额外逻辑
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        return convertToResponse(requireUser(userId));
    }

    @Override
    public UserResponse saveCsdnSession(UpdateCsdnSessionRequest request, Long userId) {
        User user = requireUser(userId);
        String normalizedCookie = normalizeCookie(request.getCookie());
        String encryptedCookie = credentialCryptoService.encrypt(normalizedCookie);
        LocalDateTime now = LocalDateTime.now();

        userMapper.updateCsdnCookie(userId, encryptedCookie, now);
        user.setCsdnCookieEncrypted(encryptedCookie);
        user.setCsdnCookieUpdateTime(now);
        return convertToResponse(user);
    }

    @Override
    public UserResponse clearCsdnSession(Long userId) {
        User user = requireUser(userId);
        userMapper.updateCsdnCookie(userId, null, null);
        user.setCsdnCookieEncrypted(null);
        user.setCsdnCookieUpdateTime(null);
        return convertToResponse(user);
    }

    @Override
    public String getCsdnSessionCookie(Long userId) {
        User user = requireUser(userId);
        if (user.getCsdnCookieEncrypted() == null || user.getCsdnCookieEncrypted().isBlank()) {
            return null;
        }
        return credentialCryptoService.decrypt(user.getCsdnCookieEncrypted());
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private String normalizeCookie(String rawCookie) {
        if (rawCookie == null || rawCookie.isBlank()) {
            throw new BusinessException(ErrorCode.CSDN_SESSION_INVALID);
        }

        String normalized = rawCookie.trim();
        if (normalized.regionMatches(true, 0, "Cookie:", 0, "Cookie:".length())) {
            normalized = normalized.substring("Cookie:".length()).trim();
        }

        normalized = normalized.replace("\r\n", "; ")
                .replace('\n', ';')
                .replace('\r', ';')
                .replaceAll("\\s*;\\s*", "; ")
                .trim();

        if (normalized.isBlank() || !normalized.contains("=")) {
            throw new BusinessException(ErrorCode.CSDN_SESSION_INVALID);
        }
        return normalized;
    }

    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setCreateTime(user.getCreateTime().format(FORMATTER));
        response.setHasCsdnSession(user.getCsdnCookieEncrypted() != null && !user.getCsdnCookieEncrypted().isBlank());
        if (user.getCsdnCookieUpdateTime() != null) {
            response.setCsdnSessionUpdateTime(user.getCsdnCookieUpdateTime().format(FORMATTER));
        }
        return response;
    }
}

