package com.ssafy.ssafy_slap.ai.dto;

import java.util.List;

public record AiScheduleDraftResponse(
        String summary,
        List<String> warnings,
        List<AiScheduleDraftItem> schedules
) {
}
