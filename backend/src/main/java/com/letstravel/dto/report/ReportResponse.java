package com.letstravel.dto.report;

import com.letstravel.domain.enums.ReportStatus;
import com.letstravel.domain.enums.ReportTargetType;

import java.time.LocalDateTime;

public record ReportResponse(
    Long id,
    Long reporterId,
    String reporterName,
    ReportTargetType targetType,
    Long targetId,
    String reason,
    String detail,
    ReportStatus status,
    LocalDateTime createdAt
) {}
