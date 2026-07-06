package com.letstravel.dto.feedback;

import jakarta.validation.constraints.*;

public record FeedbackRequest(
    @NotNull Long travelId,
    @NotNull @Min(1) @Max(5) Integer rating,
    @Size(max = 1000) String comment
) {}
