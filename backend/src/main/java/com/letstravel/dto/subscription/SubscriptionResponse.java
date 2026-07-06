package com.letstravel.dto.subscription;

import com.letstravel.domain.enums.SubscriptionStatus;
import com.letstravel.dto.travel.TravelSummary;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionResponse(
    Long id,
    TravelSummary travel,
    SubscriptionStatus status,
    LocalDateTime subscribedAt,
    LocalDateTime cancelledAt,
    PaymentSummary payment
) {
    public record PaymentSummary(
        Long id,
        BigDecimal amount,
        String currency,
        String method,
        String status,
        LocalDateTime paidAt
    ) {}
}
