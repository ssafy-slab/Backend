package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;
import com.ssafy.ssafy_slap.ai.dto.AiSuggestionResponse;
import com.ssafy.ssafy_slap.ai.mapper.AiSuggestionMapper;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.mapper.TripScheduleMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AiSuggestionService {
    private final AiSuggestionMapper suggestionMapper;
    private final TripScheduleMapper scheduleMapper;

    public AiSuggestionService(AiSuggestionMapper suggestionMapper, TripScheduleMapper scheduleMapper) {
        this.suggestionMapper = suggestionMapper;
        this.scheduleMapper = scheduleMapper;
    }

    @Transactional(readOnly = true)
    public List<AiSuggestionResponse> findSuggestions(Long tripId, Long userId, String status) {
        requireReadAccess(tripId, userId);
        return suggestionMapper.findSuggestions(tripId, normalizeStatus(status)).stream()
                .map(AiSuggestionResponse::from).toList();
    }

    @Transactional
    public AiSuggestionResponse applySuggestion(Long tripId, Long suggestionId, Long userId) {
        requireEditAccess(tripId, userId);
        rejectDirectTeamApply(tripId);
        return apply(requirePending(suggestionMapper.findSuggestionForUpdate(tripId, suggestionId)), userId);
    }

    @Transactional
    public AiSuggestionResponse rejectSuggestion(Long tripId, Long suggestionId, Long userId) {
        requireEditAccess(tripId, userId);
        AiSuggestion suggestion = requirePending(suggestionMapper.findSuggestionForUpdate(tripId, suggestionId));
        if (suggestionMapper.markRejected(suggestionId) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Suggestion is no longer pending");
        }
        return withStatus(suggestion, "REJECTED", null);
    }

    @Transactional
    public List<AiSuggestionResponse> applyRun(Long tripId, Long runId, Long userId) {
        requireEditAccess(tripId, userId);
        rejectDirectTeamApply(tripId);
        List<AiSuggestion> suggestions = suggestionMapper.findPendingSuggestionsForRunForUpdate(tripId, runId);
        if (suggestions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending suggestions found");
        }
        return suggestions.stream().map(suggestion -> apply(suggestion, userId)).toList();
    }

    @Transactional
    public List<AiSuggestionResponse> rejectRun(Long tripId, Long runId, Long userId) {
        requireEditAccess(tripId, userId);
        List<AiSuggestion> suggestions = suggestionMapper.findPendingSuggestionsForRunForUpdate(tripId, runId);
        if (suggestions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending suggestions found");
        }
        return suggestions.stream().map(suggestion -> {
            if (suggestionMapper.markRejected(suggestion.getAiSuggestionId()) != 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Suggestion is no longer pending");
            }
            return withStatus(suggestion, "REJECTED", null);
        }).toList();
    }

    private AiSuggestionResponse apply(AiSuggestion suggestion, Long userId) {
        TripScheduleItem item = new TripScheduleItem(
                null, suggestion.getTripId(), suggestion.getSuggestedPlaceId(), userId,
                suggestion.getDayNo(), suggestion.getScheduleDate(), suggestion.getStartTime(),
                suggestion.getEndTime(), suggestion.getSuggestedTitle(), suggestion.getSummary(),
                suggestion.getSortOrder(), null, null
        );
        try {
            scheduleMapper.insertScheduleItem(item);
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A schedule already exists at this time", exception);
        }
        if (suggestionMapper.markApplied(suggestion.getAiSuggestionId(), item.getScheduleItemId()) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Suggestion is no longer pending");
        }
        return withStatus(suggestion, "APPLIED", item.getScheduleItemId());
    }

    private AiSuggestion requirePending(AiSuggestion suggestion) {
        if (suggestion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AI suggestion not found");
        }
        if (!"PENDING".equals(suggestion.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "AI suggestion is not pending");
        }
        return suggestion;
    }

    private AiSuggestionResponse withStatus(AiSuggestion s, String status, Long scheduleItemId) {
        return new AiSuggestionResponse(
                s.getAiSuggestionId(), s.getAnalysisRunId(), s.getSuggestedPlaceId(),
                s.getSuggestedPlaceName(), s.getSuggestedRegionHint(), s.getSuggestedTitle(),
                s.getSummary(), s.getReason(), s.getScheduleDate(), s.getStartTime(), s.getEndTime(),
                s.getDayNo(), s.getSortOrder(), status, scheduleItemId, s.getVoteId(),
                s.getCreatedAt(), s.getAppliedAt()
        );
    }

    private void requireReadAccess(Long tripId, Long userId) {
        if (!scheduleMapper.existsAccessibleTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip is not accessible");
        }
    }

    private void requireEditAccess(Long tripId, Long userId) {
        if (!scheduleMapper.existsEditableTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip is not editable");
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) return "PENDING";
        String normalized = status.trim().toUpperCase();
        if (!List.of("PENDING", "VOTING", "APPLIED", "REJECTED").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid suggestion status");
        }
        return normalized;
    }

    private void rejectDirectTeamApply(Long tripId) {
        if ("TEAM".equalsIgnoreCase(suggestionMapper.findTripType(tripId))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Team trip suggestions must be submitted to a vote"
            );
        }
    }
}
