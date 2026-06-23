package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.domain.AiAnalysisRun;
import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftItem;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftRequest;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftResponse;
import com.ssafy.ssafy_slap.ai.mapper.AiAnalysisMapper;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import com.ssafy.ssafy_slap.trip.service.TripService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAnalysisServiceTest {

    @Test
    void buttonAnalysisPersistsRunAndSuggestionsAndReturnsStoredList() {
        AiAnalysisMapper mapper = mock(AiAnalysisMapper.class);
        TripService tripService = mock(TripService.class);
        AiScheduleClient client = mock(AiScheduleClient.class);
        AiAnalysisNotifier notifier = mock(AiAnalysisNotifier.class);
        AiPlaceMatcher placeMatcher = mock(AiPlaceMatcher.class);
        AiAnalysisService service = new AiAnalysisService(mapper, tripService, client, notifier, placeMatcher);
        List<ChatMessageResponse> messages = List.of(message(31L), message(32L));

        when(tripService.findTrip(1L, 7L)).thenReturn(trip());
        when(mapper.claimAnalysis(1L)).thenReturn(1);
        when(mapper.findUnanalyzedMessages(1L, 100)).thenReturn(messages);
        doAnswer(invocation -> {
            AiAnalysisRun run = invocation.getArgument(0);
            run.setAnalysisRunId(5L);
            return null;
        }).when(mapper).insertRun(any(AiAnalysisRun.class));
        when(client.generate(any(), any(), any())).thenReturn(new AiScheduleDraftResponse(
                "부산 일정", List.of(), List.of(new AiScheduleDraftItem(
                "해운대해수욕장", "부산 해운대구",
                LocalDate.of(2026, 7, 1), LocalTime.of(10, 0), LocalTime.of(12, 0),
                "해운대", "채팅 합의", 1, 1
        ))));
        when(placeMatcher.findPlaceId("해운대해수욕장", "부산 해운대구")).thenReturn(351L);
        doAnswer(invocation -> {
            AiSuggestion suggestion = invocation.getArgument(0);
            suggestion.setAiSuggestionId(101L);
            return null;
        }).when(mapper).insertSuggestion(any(AiSuggestion.class));

        var response = service.analyzeButton(1L, 7L, new AiScheduleDraftRequest(null, null));

        assertThat(response.analysisRunId()).isEqualTo(5L);
        assertThat(response.status()).isEqualTo("SUCCEEDED");
        assertThat(response.suggestions()).hasSize(1);
        assertThat(response.suggestions().get(0).aiSuggestionId()).isEqualTo(101L);
        assertThat(response.suggestions().get(0).suggestedPlaceId()).isEqualTo(351L);
        assertThat(response.suggestions().get(0).suggestedPlaceName()).isEqualTo("해운대해수욕장");
        assertThat(response.suggestions().get(0).suggestedRegionHint()).isEqualTo("부산 해운대구");
        assertThat(response.suggestions().get(0).reason()).isEqualTo("여행 채팅을 기반으로 생성됨");
        verify(mapper).completeState(1L, 32L);
        verify(notifier).completed(1L, 5L);
    }

    private TripResponse trip() {
        return new TripResponse(1L, 7L, "부산", null, "TEAM",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3), "PLANNING",
                LocalDateTime.now(), LocalDateTime.now());
    }

    private ChatMessageResponse message(Long id) {
        return new ChatMessageResponse(id, 1L, 7L, "여행자", "TEXT", "해운대 가자", LocalDateTime.now());
    }
}
