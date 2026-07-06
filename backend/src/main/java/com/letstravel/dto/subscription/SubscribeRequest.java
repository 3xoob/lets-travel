package com.letstravel.dto.subscription;

import com.letstravel.domain.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record SubscribeRequest(
    @NotNull Long travelId,
    @NotNull PaymentMethod paymentMethod
) {}
