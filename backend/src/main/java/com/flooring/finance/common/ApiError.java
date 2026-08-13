package com.flooring.finance.common;

import java.time.Instant;
import java.util.List;

/**
 * Consistent error body returned by {@code GlobalExceptionHandler} for every
 * failure case, so the frontend only ever has to parse one shape.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, List.of());
    }

    public static ApiError of(int status, String error, String message, String path, List<String> details) {
        return new ApiError(Instant.now(), status, error, message, path, details);
    }
}
