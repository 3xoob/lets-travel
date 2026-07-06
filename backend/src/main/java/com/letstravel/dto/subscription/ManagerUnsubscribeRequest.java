package com.letstravel.dto.subscription;

import jakarta.validation.constraints.NotBlank;

public record ManagerUnsubscribeRequest(@NotBlank String reason) {}
