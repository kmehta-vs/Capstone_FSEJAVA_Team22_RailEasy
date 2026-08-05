package com.raileasy.user.dto;

/**
 * Login response: JWT token + the authenticated user's public info.
 */
public record AuthResponse(
        String token,
        UserResponse user
) {
}
