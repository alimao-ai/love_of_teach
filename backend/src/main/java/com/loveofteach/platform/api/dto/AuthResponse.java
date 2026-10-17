package com.loveofteach.platform.api.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
