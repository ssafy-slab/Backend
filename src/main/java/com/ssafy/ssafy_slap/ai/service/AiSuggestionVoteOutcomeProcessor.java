package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;
import com.ssafy.ssafy_slap.ai.domain.AiSuggestionVote;
import com.ssafy.ssafy_slap.ai.mapper.AiSuggestionMapper;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.mapper.TripScheduleMapper;
import com.ssafy.ssafy_slap.vote.domain.VoteOption;
import com.ssafy.ssafy_slap.vote.mapper.VoteMapper;
import com.ssafy.ssafy_slap.vote.service.VoteCloseProcessor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class AiSuggestionVoteOutcomeProcessor implements VoteCloseProcessor {

    private final AiSuggestionMapper suggestionMapper;
    private final VoteMapper voteMapper;
    private final TripScheduleMapper scheduleMapper;

    public AiSuggestionVoteOutcomeProcessor(
            AiSuggestionMapper suggestionMapper,
            VoteMapper voteMapper,
            TripScheduleMapper scheduleMapper
    ) {
        this.suggestionMapper = suggestionMapper;
        this.voteMapper = voteMapper;
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    public void beforeClose(Long tripId, Long voteId, Long userId) {
        AiSuggestionVote link = suggestionMapper.findSuggestionVoteByVoteIdForUpdate(voteId);
        if (link == null) {
            return;
        }
        if (link.getResolution() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Suggestion vote is already resolved");
        }
        List<VoteOption> options = voteMapper.findOptionsWithCounts(voteId);
        long approveCount = count(options, link.getApproveOptionId());
        long rejectCount = count(options, link.getRejectOptionId());

        if (approveCount > rejectCount) {
            applySuggestion(tripId, link, userId);
            markResolved(voteId, "APPROVED");
            return;
        }
        if (suggestionMapper.markRejectedFromVoting(link.getAiSuggestionId()) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "AI suggestion is no longer voting");
        }
        markResolved(voteId, "REJECTED");
    }

    private void applySuggestion(Long tripId, AiSuggestionVote link, Long userId) {
        AiSuggestion suggestion = suggestionMapper.findSuggestionForUpdate(tripId, link.getAiSuggestionId());
        if (suggestion == null || !"VOTING".equals(suggestion.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "AI suggestion is no longer voting");
        }
        TripScheduleItem item = new TripScheduleItem(
                null, suggestion.getTripId(), suggestion.getSuggestedPlaceId(), userId,
                suggestion.getDayNo(), suggestion.getScheduleDate(), suggestion.getStartTime(),
                suggestion.getEndTime(), suggestion.getSuggestedTitle(), suggestion.getSummary(),
                suggestion.getSortOrder(), null, null
        );
        try {
            scheduleMapper.insertScheduleItem(item);
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A schedule already exists at this time", exception
            );
        }
        if (suggestionMapper.markAppliedFromVoting(link.getAiSuggestionId(), item.getScheduleItemId()) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "AI suggestion is no longer voting");
        }
    }

    private long count(List<VoteOption> options, Long optionId) {
        return options.stream()
                .filter(option -> optionId.equals(option.getVoteOptionId()))
                .map(VoteOption::getVoteCount)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(0L);
    }

    private void markResolved(Long voteId, String resolution) {
        if (suggestionMapper.markSuggestionVoteResolved(voteId, resolution) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Suggestion vote could not be resolved");
        }
    }
}
