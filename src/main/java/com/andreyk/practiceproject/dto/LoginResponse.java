package com.andreyk.practiceproject.dto;

public record LoginResponse(
        int code,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
