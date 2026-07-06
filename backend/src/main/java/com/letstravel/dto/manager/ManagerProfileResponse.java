package com.letstravel.dto.manager;

import com.letstravel.dto.travel.TravelSummary;

import java.math.BigDecimal;
import java.util.List;

public record ManagerProfileResponse(
    Long id,
    String firstName,
    String lastName,
    String bio,
    List<String> specialties,
    BigDecimal totalIncome,
    int totalTrips,
    BigDecimal averageRating,
    int reportCount,
    List<TravelSummary> recentTravels
) {}
