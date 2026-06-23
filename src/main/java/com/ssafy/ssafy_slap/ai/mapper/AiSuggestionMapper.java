package com.ssafy.ssafy_slap.ai.mapper;

import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiSuggestionMapper {
    List<AiSuggestion> findSuggestions(@Param("tripId") Long tripId, @Param("status") String status);
    AiSuggestion findSuggestionForUpdate(@Param("tripId") Long tripId, @Param("suggestionId") Long suggestionId);
    List<AiSuggestion> findPendingSuggestionsForRunForUpdate(@Param("tripId") Long tripId, @Param("runId") Long runId);
    int markApplied(@Param("suggestionId") Long suggestionId, @Param("scheduleItemId") Long scheduleItemId);
    int markRejected(@Param("suggestionId") Long suggestionId);
}
