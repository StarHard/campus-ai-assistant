package com.campus.ai.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求限流器
 * 基于滑动窗口算法实现API请求频率限制
 * 防止AI接口被滥用，保护后端服务稳定性
 *
 * @author A组长
 */
@Slf4j
@Component
public class RateLimiter {

    /** 用户请求计数器：userId -> (时间戳, 计数器) */
    private final ConcurrentHashMap<String, RateLimitInfo> userRateLimits = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.requests-per-minute:30}")
    private int requestsPerMinute;

    @Value("${app.rate-limit.requests-per-hour:300}")
    private int requestsPerHour;

    /**
     * 检查是否允许请求通过
     *
     * @param userId 用户标识（可以是IP地址或用户ID）
     * @return true-允许通过，false-被限流
     */
    public boolean allowRequest(String userId) {
        if (!enabled) {
            return true;
        }

        long now = System.currentTimeMillis();
        RateLimitInfo info = userRateLimits.computeIfAbsent(userId, k -> new RateLimitInfo());

        synchronized (info) {
            // 检查分钟级限流
            if (now - info.minuteStartTimestamp > 60_000) {
                // 新的一分钟，重置分钟计数器
                info.minuteStartTimestamp = now;
                info.minuteCount.set(0);
            }

            // 检查小时级限流
            if (now - info.hourStartTimestamp > 3_600_000) {
                // 新的一小时，重置小时计数器
                info.hourStartTimestamp = now;
                info.hourCount.set(0);
            }

            // 判断是否超限
            if (info.minuteCount.get() >= requestsPerMinute) {
                log.warn("用户 {} 触发分钟级限流: {}/{}", userId, info.minuteCount.get(), requestsPerMinute);
                return false;
            }

            if (info.hourCount.get() >= requestsPerHour) {
                log.warn("用户 {} 触发小时级限流: {}/{}", userId, info.hourCount.get(), requestsPerHour);
                return false;
            }

            // 增加计数
            info.minuteCount.incrementAndGet();
            info.hourCount.incrementAndGet();

            return true;
        }
    }

    /**
     * 获取当前用户的剩余请求数
     *
     * @param userId 用户标识
     * @return [剩余分钟请求数, 剩余小时请求数]
     */
    public int[] getRemainingRequests(String userId) {
        RateLimitInfo info = userRateLimits.get(userId);
        if (info == null) {
            return new int[]{requestsPerMinute, requestsPerHour};
        }

        synchronized (info) {
            return new int[]{
                    Math.max(0, requestsPerMinute - info.minuteCount.get()),
                    Math.max(0, requestsPerHour - info.hourCount.get())
            };
        }
    }

    /**
     * 重置指定用户的限流计数（管理员功能）
     *
     * @param userId 用户标识
     */
    public void resetUserLimit(String userId) {
        userRateLimits.remove(userId);
        log.info("已重置用户 {} 的限流计数", userId);
    }

    /**
     * 限流信息内部类
     */
    private static class RateLimitInfo {
        final AtomicLong minuteStartTimestamp = new AtomicLong(System.currentTimeMillis());
        final AtomicInteger minuteCount = new AtomicInteger(0);

        final AtomicLong hourStartTimestamp = new AtomicLong(System.currentTimeMillis());
        final AtomicInteger hourCount = new AtomicInteger(0);
    }
}
