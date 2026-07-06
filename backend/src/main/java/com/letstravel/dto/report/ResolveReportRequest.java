package com.letstravel.dto.report;

import com.letstravel.domain.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;

public record ResolveReportRequest(@NotNull ReportStatus status) {}
