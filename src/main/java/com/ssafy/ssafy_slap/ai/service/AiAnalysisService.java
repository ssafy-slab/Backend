package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.domain.AiAnalysisRun;
import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;
import com.ssafy.ssafy_slap.ai.dto.AiAnalysisResponse;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftItem;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftRequest;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftResponse;
import com.ssafy.ssafy_slap.ai.dto.AiSuggestionResponse;
import com.ssafy.ssafy_slap.ai.mapper.AiAnalysisMapper;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import com.ssafy.ssafy_slap.trip.service.TripService;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.mapper.TripScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiAnalysisService {
    private static final int AUTO_MESSAGE_COUNT = 30;
    private static final int MAX_BUTTON_MESSAGES = 100;
    private static final String DEFAULT_NO_RESULT_REASON = "NO_SCHEDULE_CONTEXT";
    private static final String DEFAULT_NO_RESULT_MESSAGE =
            "메시지가 너무 적거나 일정 관련 내용이 없어 제안을 만들지 못했습니다.";

    private final AiAnalysisMapper mapper;
    private final TripService tripService;
    private final AiScheduleClient client;
    private final AiAnalysisNotifier notifier;
    private final AiPlaceMatcher placeMatcher;
    private final TripScheduleMapper scheduleMapper;
    private final AiScheduleSlotValidator slotValidator;

    public AiAnalysisService(AiAnalysisMapper mapper, TripService tripService,
                             AiScheduleClient client, AiAnalysisNotifier notifier,
                             AiPlaceMatcher placeMatcher) {
        this(mapper, tripService, client, notifier, placeMatcher, null, new AiScheduleSlotValidator());
    }

    @Autowired
    public AiAnalysisService(
            AiAnalysisMapper mapper,
            TripService tripService,
            AiScheduleClient client,
            AiAnalysisNotifier notifier,
            AiPlaceMatcher placeMatcher,
            TripScheduleMapper scheduleMapper,
            AiScheduleSlotValidator slotValidator
    ) {
        this.mapper = mapper;
        this.tripService = tripService;
        this.client = client;
        this.notifier = notifier;
        this.placeMatcher = placeMatcher;
        this.scheduleMapper = scheduleMapper;
        this.slotValidator = slotValidator;
    }

    public AiAnalysisResponse analyzeButton(Long tripId, Long userId, AiScheduleDraftRequest request) {
        int limit = request == null || request.messageLimit() == null
                ? MAX_BUTTON_MESSAGES : request.messageLimit();
        return analyze(tripId, userId, userId, "BUTTON", limit,
                request == null ? null : request.additionalRequest());
    }

    public boolean shouldAutoAnalyze(Long tripId) {
        mapper.ensureState(tripId);
        return mapper.countUnanalyzedMessages(tripId) >= AUTO_MESSAGE_COUNT;
    }

    public AiAnalysisResponse analyzeAuto(Long tripId, Long accessUserId) {
        return analyze(tripId, accessUserId, null, "AUTO", AUTO_MESSAGE_COUNT, null);
    }

    private AiAnalysisResponse analyze(Long tripId, Long accessUserId, Long requestedByUserId,
                                       String triggerType, int limit, String additionalRequest) {
        TripResponse trip = tripService.findTrip(tripId, accessUserId);
        mapper.ensureState(tripId);
        if (mapper.claimAnalysis(tripId) != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "AI analysis is already running");
        }
        List<ChatMessageResponse> messages = mapper.findUnanalyzedMessages(tripId, limit);
        if (messages.isEmpty()) {
            mapper.releaseState(tripId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There are no new chat messages to analyze");
        }

        AiAnalysisRun run = new AiAnalysisRun(
                null, tripId, requestedByUserId, triggerType,
                messages.get(0).messageId(), messages.get(messages.size() - 1).messageId(),
                messages.size(), "RUNNING", null, null, null
        );
        mapper.insertRun(run);
        log.info("AI analysis run started: tripId={}, runId={}, triggerType={}, messageCount={}, firstMessageId={}, lastMessageId={}",
                tripId, run.getAnalysisRunId(), triggerType, messages.size(), run.getFirstMessageId(), run.getLastMessageId());

        try {
            List<TripScheduleItem> existingSchedules = scheduleMapper == null
                    ? List.of()
                    : scheduleMapper.findScheduleItemsByTripId(tripId);
            AiScheduleDraftResponse draft = scheduleMapper == null
                    ? client.generate(trip, messages, normalize(additionalRequest))
                    : client.generate(trip, messages, existingSchedules, normalize(additionalRequest));
            if (draft != null && draft.isNoResult()) {
                String reasonCode = normalizeOrDefault(draft.reasonCode(), DEFAULT_NO_RESULT_REASON);
                String message = normalizeOrDefault(draft.message(), DEFAULT_NO_RESULT_MESSAGE);
                mapper.markRunSucceeded(run.getAnalysisRunId());
                mapper.completeState(tripId, run.getLastMessageId());
                notifier.noResult(tripId, run.getAnalysisRunId(), reasonCode, message);
                log.info("AI analysis produced no suggestions: tripId={}, runId={}, reasonCode={}",
                        tripId, run.getAnalysisRunId(), reasonCode);
                return new AiAnalysisResponse(run.getAnalysisRunId(), triggerType, "NO_RESULT", List.of());
            }
            draft = slotValidator.normalizeAndValidate(draft, trip, existingSchedules);
            List<AiSuggestionResponse> suggestions = persistSuggestions(run, draft);
            mapper.markRunSucceeded(run.getAnalysisRunId());
            mapper.completeState(tripId, run.getLastMessageId());
            notifier.completed(tripId, run.getAnalysisRunId());
            log.info("AI analysis run succeeded: tripId={}, runId={}, suggestionCount={}",
                    tripId, run.getAnalysisRunId(), suggestions.size());
            return new AiAnalysisResponse(run.getAnalysisRunId(), triggerType, "SUCCEEDED", suggestions);
        } catch (RuntimeException exception) {
            mapper.markRunFailed(run.getAnalysisRunId(), truncate(exception.getMessage()));
            mapper.failState(tripId);
            log.error("AI analysis run failed: tripId={}, runId={}, triggerType={}, messageCount={}",
                    tripId, run.getAnalysisRunId(), triggerType, messages.size(), exception);
            throw exception;
        }
    }

    private List<AiSuggestionResponse> persistSuggestions(AiAnalysisRun run, AiScheduleDraftResponse draft) {
        if (draft == null || draft.schedules() == null || draft.schedules().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned no schedule suggestions");
        }
        List<AiSuggestionResponse> responses = new ArrayList<>();
        for (AiScheduleDraftItem item : draft.schedules()) {
            if (item.scheduleDate() == null || item.startTime() == null
                    || item.title() == null || item.title().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI returned an incomplete suggestion");
            }
            String placeName = normalize(item.placeName());
            String regionHint = normalize(item.regionHint());
            Long suggestedPlaceId = placeMatcher.findPlaceId(placeName, regionHint);
            AiSuggestion suggestion = new AiSuggestion(
                    null, run.getAnalysisRunId(), run.getTripId(), suggestedPlaceId,
                    placeName, regionHint, "SCHEDULE",
                    item.title().trim(), item.memo(), "여행 채팅을 기반으로 생성됨",
                    item.scheduleDate(), item.startTime(), item.endTime(), item.dayNo(),
                    item.sortOrder(), "PENDING", null, null, null
            );
            mapper.insertSuggestion(suggestion);
            responses.add(AiSuggestionResponse.from(suggestion));
        }
        return responses;
    }

    private String normalize(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    private String normalizeOrDefault(String text, String defaultValue) {
        String normalized = normalize(text);
        return normalized == null ? defaultValue : normalized;
    }

    private String truncate(String message) {
        if (message == null) return "Unknown AI analysis failure";
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }
}
