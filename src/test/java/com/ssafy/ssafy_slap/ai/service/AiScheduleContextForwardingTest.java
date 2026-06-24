package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftItem;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftRequest;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftResponse;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.chat.service.ChatService;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import com.ssafy.ssafy_slap.trip.mapper.TripScheduleMapper;
import com.ssafy.ssafy_slap.trip.service.TripService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiScheduleContextForwardingTest {

    @Test
    void draftGenerationPassesExistingSchedulesToAiClient() {
        TripService tripService = mock(TripService.class);
        ChatService chatService = mock(ChatService.class);
        AiScheduleClient client = mock(AiScheduleClient.class);
        TripScheduleMapper scheduleMapper = mock(TripScheduleMapper.class);
        var service = new AiScheduleDraftService(
                tripService, chatService, client, scheduleMapper, new AiScheduleSlotValidator()
        );
        TripResponse trip = trip();
        List<ChatMessageResponse> messages = List.of(message());
        List<TripScheduleItem> schedules = List.of(existing());
        AiScheduleDraftResponse response = new AiScheduleDraftResponse(
                "일정", List.of(), List.of(new AiScheduleDraftItem(
                null, null, LocalDate.of(2026, 7, 1), LocalTime.of(11, 0),
                LocalTime.of(12, 0), "새 일정", null, 1, 1
        )));

        when(tripService.findTrip(1L, 7L)).thenReturn(trip);
        when(chatService.findRecentMessages(7L, 1L, 100)).thenReturn(messages);
        when(scheduleMapper.findScheduleItemsByTripId(1L)).thenReturn(schedules);
        when(client.generate(trip, messages, schedules, null)).thenReturn(response);

        service.generateDraft(1L, 7L, new AiScheduleDraftRequest(null, null));

        verify(client).generate(trip, messages, schedules, null);
    }

    private TripResponse trip() {
        return new TripResponse(
                1L, 7L, "부산 여행", null, "TEAM",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3),
                "PLANNING", LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private ChatMessageResponse message() {
        return new ChatMessageResponse(
                11L, 1L, 7L, "여행자", "TEXT", "카페 가자", LocalDateTime.now()
        );
    }

    private TripScheduleItem existing() {
        return new TripScheduleItem(
                91L, 1L, null, 7L, 1, LocalDate.of(2026, 7, 1),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                "기존 일정", null, 1, null, null
        );
    }
}
