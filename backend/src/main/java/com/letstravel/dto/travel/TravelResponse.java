package com.letstravel.dto.travel;

import com.letstravel.domain.enums.TravelCategory;
import com.letstravel.domain.enums.TravelStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TravelResponse(
    Long id,
    String title,
    String description,
    String destinationCity,
    String destinationCountry,
    BigDecimal destinationLatitude,
    BigDecimal destinationLongitude,
    TravelCategory category,
    List<String> tags,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal price,
    int capacity,
    int currentEnrollment,
    List<String> imageUrls,
    TravelStatus status,
    ManagerSummary manager,
    BigDecimal averageRating,
    long feedbackCount,
    boolean canSubscribe,
    boolean canUnsubscribe,
    LocalDateTime createdAt
) {
    public record ManagerSummary(Long id, String firstName, String lastName, BigDecimal averageRating) {}
}
