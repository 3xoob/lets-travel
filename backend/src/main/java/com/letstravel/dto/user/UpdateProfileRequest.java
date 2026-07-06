package com.letstravel.dto.user;

public record UpdateProfileRequest(
    String firstName,
    String lastName,
    String avatarUrl
) {}
