package com.letstravel.dto.travel;

import com.letstravel.domain.enums.TravelCategory;
import com.letstravel.domain.enums.TravelStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TravelRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String destination,
    @NotBlank String country,
    @NotNull TravelCategory category,
    @NotNull @DecimalMin("0.01") BigDecimal price,
    @NotNull @Min(1) Integer capacity,
    @NotNull @Future LocalDate startDate,
    @NotNull LocalDate endDate,
    List<String> tags,
    TravelStatus status,
    Double latitude,
    Double longitude
) {}
