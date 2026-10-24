package com.loveofteach.platform.service;

import com.loveofteach.platform.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limits repeated login failures per account and source address in this process.
 * A distributed deployment can replace this with a shared rate-limit store.
 */
@Service
public class LoginAttemptService {

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    @Value("${app.auth.login-max-failures:5}")
    private int maxFailures;

    @Value("${app.auth.login-block-minutes:15}")
    private long blockMinutes;

    public void assertAllowed(String key) {
        Instant now = Instant.now();
        AttemptWindow window = attempts.get(key);
        if (window == null) {
            return;
        }
        if (window.blockedUntil() != null && window.blockedUntil().isAfter(now)) {
            throw new BusinessException(
                    "登录失败次数过多，请稍后重试", HttpStatus.TOO_MANY_REQUESTS);
        }
        if (window.blockedUntil() != null || windowExpired(window, now)) {
            attempts.remove(key, window);
        }
    }

    public void recordFailure(String key) {
        Instant now = Instant.now();
        attempts.compute(key, (ignored, current) -> {
            boolean reset = current == null
                    || current.blockedUntil() != null
                    || windowExpired(current, now);
            int failures = reset
                    ? 1
                    : current.failures() + 1;
            Instant blockedUntil = failures >= maxFailures
                    ? now.plus(blockDuration())
                    : null;
            return new AttemptWindow(failures, reset ? now : current.windowStart(), blockedUntil);
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    public void clear() {
        attempts.clear();
    }

    private boolean windowExpired(AttemptWindow window, Instant now) {
        return !now.isBefore(window.windowStart().plus(blockDuration()));
    }

    private java.time.Duration blockDuration() {
        return java.time.Duration.ofMinutes(blockMinutes);
    }

    private record AttemptWindow(
            int failures,
            Instant windowStart,
            Instant blockedUntil
    ) {
    }
}
