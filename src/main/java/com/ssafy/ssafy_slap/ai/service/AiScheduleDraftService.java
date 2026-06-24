package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftItem;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftRequest;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftResponse;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.chat.service.ChatService;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import com.ssafy.ssafy_slap.trip.service.TripService;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.mapper.TripScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AiScheduleDraftService {

    private static final int DEFAULT_MESSAGE_LIMIT = 100;

    private final TripService tripService;
    private final ChatService chatService;
    private final AiScheduleClient aiScheduleClient;
    private final TripScheduleMapper scheduleMapper;
    private final AiScheduleSlotValidator slotValidator;

    public AiScheduleDraftService(
            TripService tripService,
            ChatService chatService,
            AiScheduleClient aiScheduleClient
    ) {
        this(tripService, chatService, aiScheduleClient, null, new AiScheduleSlotValidator());
    }

    @Autowired
    public AiScheduleDraftService(
            TripService tripService,
            ChatService chatService,
            AiScheduleClient aiScheduleClient,
            TripScheduleMapper scheduleMapper,
            AiScheduleSlotValidator slotValidator
    ) {
        this.tripService = tripService;
        this.chatService = chatService;
        this.aiScheduleClient = aiScheduleClient;
        this.scheduleMapper = scheduleMapper;
        this.slotValidator = slotValidator;
    }

    public AiScheduleDraftResponse generateDraft(
            Long tripId,
            Long userId,
            AiScheduleDraftRequest request
    ) {
        int messageLimit = request == null || request.messageLimit() == null
                ? DEFAULT_MESSAGE_LIMIT
                : request.messageLimit();
        if (messageLimit < 1 || messageLimit > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "messageLimit must be between 1 and 100");
        }

        TripResponse trip = tripService.findTrip(tripId, userId);
        List<ChatMessageResponse> messages = chatService.findRecentMessages(userId, tripId, messageLimit)
                .stream()
                .filter(message -> "TEXT".equalsIgnoreCase(message.messageType()))
                .filter(message -> message.content() != null && !message.content().isBlank())
                .toList();
        if (messages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one text chat message is required");
        }

        List<TripScheduleItem> existingSchedules = scheduleMapper == null
                ? List.of()
                : scheduleMapper.findScheduleItemsByTripId(tripId);
        String additionalRequest = normalizeText(request == null ? null : request.additionalRequest());
        AiScheduleDraftResponse draft = scheduleMapper == null
                ? aiScheduleClient.generate(trip, messages, additionalRequest)
                : aiScheduleClient.generate(trip, messages, existingSchedules, additionalRequest);
        draft = slotValidator.normalizeAndValidate(draft, trip, existingSchedules);
        validateDraft(draft, trip);
        return new AiScheduleDraftResponse(
                normalizeText(draft.summary()),
                draft.warnings() == null ? List.of() : List.copyOf(draft.warnings()),
                List.copyOf(draft.schedules())
        );
    }

    private void validateDraft(AiScheduleDraftResponse draft, TripResponse trip) {
        if (draft == null || draft.schedules() == null || draft.schedules().isEmpty()) {
            throw invalidAiResponse("AI returned no schedule items");
        }
        for (AiScheduleDraftItem item : draft.schedules()) {
            if (item == null
                    || item.scheduleDate() == null
                    || item.startTime() == null
                    || normalizeText(item.title()) == null) {
                throw invalidAiResponse("AI returned an incomplete schedule item");
            }
            if (outsideTripDateRange(item.scheduleDate(), trip.startDate(), trip.endDate())) {
                throw invalidAiResponse("AI returned a schedule date outside the trip range");
            }
        }
    }

    private boolean outsideTripDateRange(LocalDate scheduleDate, LocalDate startDate, LocalDate endDate) {
        return startDate != null && scheduleDate.isBefore(startDate)
                || endDate != null && scheduleDate.isAfter(endDate);
    }

    private ResponseStatusException invalidAiResponse(String message) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
