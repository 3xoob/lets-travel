package com.letstravel.dto.report;

import com.letstravel.domain.enums.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportRequest(
    @NotNull ReportTargetType targetType,
    @NotNull Long targetId,
    @NotBlank String reason,
    String detail
) {}
