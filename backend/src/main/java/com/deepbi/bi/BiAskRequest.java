package com.deepbi.bi;

import jakarta.validation.constraints.NotBlank;

public record BiAskRequest(
        @NotBlank(message = "question is required")
        String question,
        String sessionId
) {
}

