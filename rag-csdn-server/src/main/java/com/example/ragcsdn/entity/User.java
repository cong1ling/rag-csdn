package com.example.ragcsdn.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    /**
     * 用户主键
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（BCrypt 加密）
     */
    private String password;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 加密后的 CSDN 登录态 Cookie
     */
    private String csdnCookieEncrypted;

    /**
     * CSDN 登录态更新时间
     */
    private LocalDateTime csdnCookieUpdateTime;
}

