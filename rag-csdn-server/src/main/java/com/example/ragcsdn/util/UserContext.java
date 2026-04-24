package com.example.ragcsdn.util;

/**
 * 用户上下文，基于 ThreadLocal 存储当前请求的用户 ID
 */
public final class UserContext {

    private UserContext() {}

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void set(Long userId) {
        USER_ID.set(userId);
    }

    public static Long get() {
        return USER_ID.get();
    }

    public static void remove() {
        USER_ID.remove();
    }
}

