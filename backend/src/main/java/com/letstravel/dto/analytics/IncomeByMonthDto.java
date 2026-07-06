package com.letstravel.dto.analytics;

import java.math.BigDecimal;

public record IncomeByMonthDto(
    int year,
    int month,
    BigDecimal totalAmount,
    long subscriptionCount
) {}
