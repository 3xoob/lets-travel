package com.letstravel.dto.payment;

public record PaymentInitResponse(
    Long subscriptionId,
    Long paymentId,
    String method,
    String clientSecret,
    String approvalUrl
) {}
