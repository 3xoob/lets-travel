package com.letstravel.dto.analytics;

import com.letstravel.dto.travel.TravelSummary;

import java.math.BigDecimal;
import java.util.List;

public record ManagerDashboardResponse(
    BigDecimal currentMonthIncome,
    BigDecimal prevMonthIncome,
    long activeTravels,
    long totalTravels,
    long totalSubscribers,
    BigDecimal averageRating,
    List<TravelWithStats> travels
) {
    public record TravelWithStats(
        TravelSummary travel,
        long subscriberCount,
        BigDecimal income,
        BigDecimal averageRating
    ) {}
}
