package com.example.ragbilibili.util;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP 级内存限流工具
 */
public class RateLimiter {

    // 注册：每个 IP 每小时最多 3 次
    private static final ConcurrentHashMap<String, Bucket> REGISTER_BUCKETS = new ConcurrentHashMap<>();

    // 登录：每个 IP 每分钟最多 5 次
    private static final ConcurrentHashMap<String, Bucket> LOGIN_BUCKETS = new ConcurrentHashMap<>();

    public static boolean allowRegister(String ip) {
        Bucket bucket = REGISTER_BUCKETS.computeIfAbsent(ip, k ->
                Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(3)
                                .refillGreedy(3, Duration.ofHours(1))
                                .build())
                        .build());
        return bucket.tryConsume(1);
    }

    public static boolean allowLogin(String ip) {
        Bucket bucket = LOGIN_BUCKETS.computeIfAbsent(ip, k ->
                Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(5)
                                .refillGreedy(5, Duration.ofMinutes(1))
                                .build())
                        .build());
        return bucket.tryConsume(1);
    }
}
