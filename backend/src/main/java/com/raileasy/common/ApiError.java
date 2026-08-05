package com.raileasy.common;

import java.time.Instant;

/**
 * Consistent error response body used across the API.
 *
 * <pre>
 * {
 *   "timestamp": "2025-09-12T12:00:00Z",
 *   "path": "/api/...",
 *   "error": "VALIDATION_ERROR",
 *   "message": "Field is required"
 * }
 * </pre>
 */
public record ApiError(
        String timestamp,
        String path,
        String error,
        String message
) {
    public static ApiError of(String path, String error, String message) {
        return new ApiError(Instant.now().toString(), path, error, message);
    }
}
