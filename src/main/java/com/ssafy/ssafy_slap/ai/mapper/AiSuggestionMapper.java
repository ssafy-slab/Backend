package com.ssafy.ssafy_slap.ai.mapper;

import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;
import com.ssafy.ssafy_slap.ai.domain.AiSuggestionVote;
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
    String findTripType(@Param("tripId") Long tripId);
    AiSuggestionVote findSuggestionVote(@Param("suggestionId") Long suggestionId);
    AiSuggestionVote findSuggestionVoteByVoteIdForUpdate(@Param("voteId") Long voteId);
    void insertSuggestionVote(
            @Param("suggestionId") Long suggestionId,
            @Param("voteId") Long voteId,
            @Param("approveOptionId") Long approveOptionId,
            @Param("rejectOptionId") Long rejectOptionId
    );
    int markVoting(@Param("suggestionId") Long suggestionId);
    int markAppliedFromVoting(
            @Param("suggestionId") Long suggestionId,
            @Param("scheduleItemId") Long scheduleItemId
    );
    int markRejectedFromVoting(@Param("suggestionId") Long suggestionId);
    int markSuggestionVoteResolved(
            @Param("voteId") Long voteId,
            @Param("resolution") String resolution
    );
}
