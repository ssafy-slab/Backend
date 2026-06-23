package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftItem;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftRequest;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftResponse;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.chat.service.ChatService;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import com.ssafy.ssafy_slap.trip.service.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiScheduleDraftServiceTest {

    @Test
    void generatesDraftFromAccessibleTripChatWithoutPersistingSchedules() {
        TripService tripService = mock(TripService.class);
        ChatService chatService = mock(ChatService.class);
        AiScheduleClient aiClient = mock(AiScheduleClient.class);
        AiScheduleDraftService service = new AiScheduleDraftService(tripService, chatService, aiClient);
        TripResponse trip = trip();
        List<ChatMessageResponse> messages = List.of(message("첫날 오전에는 해운대 가자"));
        AiScheduleDraftResponse generated = new AiScheduleDraftResponse(
                "부산 여행 초안",
                List.of(),
                List.of(new AiScheduleDraftItem(
                        "해운대해수욕장",
                        "부산 해운대구",
                        LocalDate.of(2026, 7, 1),
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0),
                        "해운대 방문",
                        "채팅 합의",
                        1,
                        1
                ))
        );

        when(tripService.findTrip(1L, 7L)).thenReturn(trip);
        when(chatService.findRecentMessages(7L, 1L, 80)).thenReturn(messages);
        when(aiClient.generate(trip, messages, "이동 시간을 짧게")).thenReturn(generated);

        AiScheduleDraftResponse response = service.generateDraft(
                1L,
                7L,
                new AiScheduleDraftRequest(80, "  이동 시간을 짧게  ")
        );

        assertThat(response.summary()).isEqualTo("부산 여행 초안");
        assertThat(response.schedules()).hasSize(1);
        assertThat(response.schedules().get(0).placeName()).isEqualTo("해운대해수욕장");
        verify(aiClient).generate(trip, messages, "이동 시간을 짧게");
    }

    @Test
    void rejectsDraftGenerationWhenChatIsEmpty() {
        TripService tripService = mock(TripService.class);
        ChatService chatService = mock(ChatService.class);
        AiScheduleClient aiClient = mock(AiScheduleClient.class);
        AiScheduleDraftService service = new AiScheduleDraftService(tripService, chatService, aiClient);

        when(tripService.findTrip(1L, 7L)).thenReturn(trip());
        when(chatService.findRecentMessages(7L, 1L, 100)).thenReturn(List.of());

        assertThatThrownBy(() -> service.generateDraft(1L, 7L, new AiScheduleDraftRequest(null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsGeneratedScheduleOutsideTripDateRange() {
        TripService tripService = mock(TripService.class);
        ChatService chatService = mock(ChatService.class);
        AiScheduleClient aiClient = mock(AiScheduleClient.class);
        AiScheduleDraftService service = new AiScheduleDraftService(tripService, chatService, aiClient);
        TripResponse trip = trip();
        List<ChatMessageResponse> messages = List.of(message("마지막 날 해운대"));

        when(tripService.findTrip(1L, 7L)).thenReturn(trip);
        when(chatService.findRecentMessages(7L, 1L, 100)).thenReturn(messages);
        when(aiClient.generate(trip, messages, null)).thenReturn(new AiScheduleDraftResponse(
                "잘못된 초안",
                List.of(),
                List.of(new AiScheduleDraftItem(
                        "해운대해수욕장",
                        "부산 해운대구",
                        LocalDate.of(2026, 7, 10),
                        LocalTime.of(10, 0),
                        null,
                        "해운대",
                        null,
                        10,
                        1
                ))
        ));

        assertThatThrownBy(() -> service.generateDraft(1L, 7L, new AiScheduleDraftRequest(null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    private TripResponse trip() {
        return new TripResponse(
                1L,
                7L,
                "부산 여행",
                null,
                "TEAM",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                "PLANNING",
                LocalDateTime.of(2026, 6, 23, 10, 0),
                LocalDateTime.of(2026, 6, 23, 10, 0)
        );
    }

    private ChatMessageResponse message(String content) {
        return new ChatMessageResponse(
                11L,
                1L,
                7L,
                "여행자",
                "TEXT",
                content,
                LocalDateTime.of(2026, 6, 23, 11, 0)
        );
    }
}
