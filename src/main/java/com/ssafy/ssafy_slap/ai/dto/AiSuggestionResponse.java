package com.ssafy.ssafy_slap.ai.dto;

import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AiSuggestionResponse(
        Long aiSuggestionId,
        Long analysisRunId,
        Long suggestedPlaceId,
        String suggestedPlaceName,
        String suggestedRegionHint,
        String title,
        String summary,
        String reason,
        LocalDate scheduleDate,
        LocalTime startTime,
        LocalTime endTime,
        Integer dayNo,
        Integer sortOrder,
        String status,
        Long appliedScheduleItemId,
        Long voteId,
        LocalDateTime createdAt,
        LocalDateTime appliedAt
) {
    public static AiSuggestionResponse from(AiSuggestion suggestion) {
        return new AiSuggestionResponse(
                suggestion.getAiSuggestionId(), suggestion.getAnalysisRunId(),
                suggestion.getSuggestedPlaceId(), suggestion.getSuggestedPlaceName(),
                suggestion.getSuggestedRegionHint(), suggestion.getSuggestedTitle(),
                suggestion.getSummary(), suggestion.getReason(), suggestion.getScheduleDate(),
                suggestion.getStartTime(), suggestion.getEndTime(), suggestion.getDayNo(),
                suggestion.getSortOrder(), suggestion.getStatus(),
                suggestion.getAppliedScheduleItemId(), suggestion.getVoteId(),
                suggestion.getCreatedAt(), suggestion.getAppliedAt()
        );
    }
}
