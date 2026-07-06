package com.letstravel.dto.feedback;

import java.time.LocalDateTime;

public record FeedbackResponse(
    Long id,
    Long travelId,
    String travelTitle,
    String travelerDisplayName,
    int rating,
    String comment,
    LocalDateTime createdAt
) {}
