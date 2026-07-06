package com.letstravel.dto.traveler;

import java.math.BigDecimal;

public record TravelerStatsResponse(
    long pastTrips,
    long upcomingTrips,
    BigDecimal totalSpend,
    long cancellations,
    long reviewsGiven,
    long reportsFiled
) {}
