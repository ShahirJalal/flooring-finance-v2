package com.flooring.finance.security;

import com.flooring.finance.exception.TooManyAttemptsException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * A simple in-memory lockout after repeated failed logins for a username.
 * Deliberately not backed by a shared store (Redis etc.) - this app runs as
 * a single instance, so per-JVM state is enough, and it resets on restart,
 * which is an acceptable trade-off for a single-owner internal tool.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Attempts> attemptsByUsername = new ConcurrentHashMap<>();

    /** Call before attempting authentication - throws if this username is currently locked out. */
    public void checkAllowed(String username) {
        Attempts attempts = attemptsByUsername.get(normalize(username));
        if (attempts != null && attempts.isBlocked()) {
            throw new TooManyAttemptsException("Too many failed login attempts. Try again in a few minutes.");
        }
    }

    public void recordFailure(String username) {
        attemptsByUsername.compute(normalize(username), (key, existing) -> {
            Attempts attempts = (existing == null || existing.isExpired()) ? new Attempts() : existing;
            attempts.count++;
            return attempts;
        });
    }

    public void recordSuccess(String username) {
        attemptsByUsername.remove(normalize(username));
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private static final class Attempts {
        private int count = 0;
        private final Instant windowStart = Instant.now();

        boolean isExpired() {
            return Instant.now().isAfter(windowStart.plus(WINDOW));
        }

        boolean isBlocked() {
            return !isExpired() && count >= MAX_ATTEMPTS;
        }
    }
}
