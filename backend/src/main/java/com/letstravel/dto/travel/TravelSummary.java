package com.letstravel.dto.travel;

import com.letstravel.domain.enums.TravelCategory;
import com.letstravel.domain.enums.TravelStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TravelSummary(
    Long id,
    String title,
    String destinationCity,
    String destinationCountry,
    TravelCategory category,
    List<String> tags,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal price,
    int capacity,
    int currentEnrollment,
    List<String> imageUrls,
    TravelStatus status,
    BigDecimal averageRating
) {}
