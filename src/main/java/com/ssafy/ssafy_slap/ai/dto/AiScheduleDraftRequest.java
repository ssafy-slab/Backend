package com.ssafy.ssafy_slap.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record AiScheduleDraftRequest(
        @Min(1) @Max(100) Integer messageLimit,
        @Size(max = 1000) String additionalRequest
) {
}
