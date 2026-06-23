package com.ssafy.ssafy_slap.ai.mapper;

import com.ssafy.ssafy_slap.ai.domain.AiAnalysisRun;
import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiAnalysisMapper {
    void ensureState(@Param("tripId") Long tripId);
    int claimAnalysis(@Param("tripId") Long tripId);
    int countUnanalyzedMessages(@Param("tripId") Long tripId);
    List<ChatMessageResponse> findUnanalyzedMessages(@Param("tripId") Long tripId, @Param("limit") int limit);
    void insertRun(@Param("run") AiAnalysisRun run);
    void insertSuggestion(@Param("suggestion") AiSuggestion suggestion);
    int markRunSucceeded(@Param("runId") Long runId);
    int markRunFailed(@Param("runId") Long runId, @Param("errorMessage") String errorMessage);
    int completeState(@Param("tripId") Long tripId, @Param("lastMessageId") Long lastMessageId);
    int failState(@Param("tripId") Long tripId);
    int releaseState(@Param("tripId") Long tripId);
}
