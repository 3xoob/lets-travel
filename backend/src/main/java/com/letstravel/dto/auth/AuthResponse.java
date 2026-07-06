package com.letstravel.dto.auth;

import com.letstravel.dto.user.UserDto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserDto user
) {}
