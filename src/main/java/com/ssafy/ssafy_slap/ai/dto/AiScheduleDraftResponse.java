package com.ssafy.ssafy_slap.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

public record AiScheduleDraftResponse(
        String summary,
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<String> warnings,
        List<AiScheduleDraftItem> schedules
) {
}
