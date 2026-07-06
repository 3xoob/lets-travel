package com.letstravel.dto.user;

import com.letstravel.domain.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull UserRole role) {}
