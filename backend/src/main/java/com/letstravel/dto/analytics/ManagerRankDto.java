package com.letstravel.dto.analytics;

import java.math.BigDecimal;

public record ManagerRankDto(
    Long userId,
    String firstName,
    String lastName,
    BigDecimal averageRating,
    BigDecimal totalIncome,
    int totalTrips,
    int reportCount,
    BigDecimal score
) {}
