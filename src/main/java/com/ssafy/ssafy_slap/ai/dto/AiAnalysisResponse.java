package com.ssafy.ssafy_slap.ai.dto;

import java.util.List;

public record AiAnalysisResponse(
        Long analysisRunId,
        String triggerType,
        String status,
        List<AiSuggestionResponse> suggestions
) {
}
