package com.raileasy.user.dto;

import com.raileasy.user.User;

import java.util.UUID;

/**
 * Public user representation (never exposes the password hash).
 */
public record UserResponse(
        UUID id,
        String email,
        String name,
        boolean isAdmin
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.isAdmin());
    }
}
