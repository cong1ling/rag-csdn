package com.example.ragbilibili.exception;

import lombok.Getter;

/**
 * 错误码枚举
 *
 * 每个错误码绑定了业务 code、描述信息、以及对应的 HTTP 状态码，
 * 使 API 网关和监控系统能够正确识别错误类型。
 */
@Getter
public enum ErrorCode {
    // 通用错误
    SUCCESS(200, "操作成功", 200),
    SYSTEM_ERROR(500, "系统错误，请联系管理员", 500),
    PARAM_ERROR(400, "参数错误", 400),

    // 认证相关
    NOT_LOGGED_IN(401, "未登录或登录已过期", 401),
    USERNAME_EXISTS(409, "用户名已存在", 409),
    USER_NOT_FOUND(404, "用户不存在", 404),
    PASSWORD_ERROR(401, "密码错误", 401),
    REGISTER_DISABLED(403, "注册功能已关闭", 403),
    RATE_LIMIT_EXCEEDED(429, "操作过于频繁，请稍后重试", 429),

    // 视频相关
    VIDEO_NOT_FOUND(1001, "视频不存在", 404),
    VIDEO_ALREADY_EXISTS(1002, "该视频已导入", 409),
    VIDEO_IMPORT_FAILED(1003, "视频导入失败", 500),
    VIDEO_NO_SUBTITLE(1004, "视频无字幕内容", 422),

    // 会话相关
    SESSION_NOT_FOUND(2001, "会话不存在", 404),

    // 消息相关
    MESSAGE_SEND_FAILED(3001, "消息发送失败", 500);

    private final int code;
    private final String message;
    private final int httpStatus;

    ErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
