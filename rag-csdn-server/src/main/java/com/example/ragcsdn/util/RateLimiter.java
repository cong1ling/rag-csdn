package com.example.ragcsdn.util;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * IP 级内存限流工具
 * <p>
 * 内置自动清理机制：超过 {@value EVICT_AFTER_MINUTES} 分钟未被访问的桶会被回收，
 * 防止在公网部署场景下因大量不同 IP 导致内存无限增长。
 */
public class RateLimiter {

    private static final long EVICT_AFTER_MINUTES = 60;
    private static final long CLEANUP_INTERVAL_MINUTES = 10;

    // 注册：每个 IP 每小时最多 3 次
    private static final ConcurrentHashMap<String, TimedBucket> REGISTER_BUCKETS = new ConcurrentHashMap<>();

    // 登录：每个 IP 每分钟最多 5 次
    private static final ConcurrentHashMap<String, TimedBucket> LOGIN_BUCKETS = new ConcurrentHashMap<>();

    static {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limiter-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(
                () -> evictExpired(REGISTER_BUCKETS),
                CLEANUP_INTERVAL_MINUTES, CLEANUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
        cleaner.scheduleAtFixedRate(
                () -> evictExpired(LOGIN_BUCKETS),
                CLEANUP_INTERVAL_MINUTES, CLEANUP_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    public static boolean allowRegister(String ip) {
        TimedBucket tb = REGISTER_BUCKETS.computeIfAbsent(ip, k ->
                new TimedBucket(Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(3)
                                .refillGreedy(3, Duration.ofHours(1))
                                .build())
                        .build()));
        tb.touch();
        return tb.bucket.tryConsume(1);
    }

    public static boolean allowLogin(String ip) {
        TimedBucket tb = LOGIN_BUCKETS.computeIfAbsent(ip, k ->
                new TimedBucket(Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(5)
                                .refillGreedy(5, Duration.ofMinutes(1))
                                .build())
                        .build()));
        tb.touch();
        return tb.bucket.tryConsume(1);
    }

    private static void evictExpired(ConcurrentHashMap<String, TimedBucket> map) {
        long threshold = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(EVICT_AFTER_MINUTES);
        map.entrySet().removeIf(entry -> entry.getValue().lastAccessTime < threshold);
    }

    private static class TimedBucket {
        final Bucket bucket;
        volatile long lastAccessTime;

        TimedBucket(Bucket bucket) {
            this.bucket = bucket;
            this.lastAccessTime = System.currentTimeMillis();
        }

        void touch() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
}

