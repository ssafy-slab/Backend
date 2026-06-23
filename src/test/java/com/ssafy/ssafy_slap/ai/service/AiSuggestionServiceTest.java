package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.domain.AiSuggestion;
import com.ssafy.ssafy_slap.ai.mapper.AiSuggestionMapper;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.mapper.TripScheduleMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiSuggestionServiceTest {

    @Test
    void appliesPendingSuggestionAsScheduleAndMarksItApplied() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        TripScheduleMapper scheduleMapper = mock(TripScheduleMapper.class);
        AiSuggestionService service = new AiSuggestionService(suggestionMapper, scheduleMapper);
        AiSuggestion suggestion = suggestion(11L, "PENDING");

        when(scheduleMapper.existsEditableTrip(1L, 7L)).thenReturn(true);
        when(suggestionMapper.findSuggestionForUpdate(1L, 11L)).thenReturn(suggestion);
        doAnswer(invocation -> {
            TripScheduleItem item = invocation.getArgument(0);
            item.setScheduleItemId(99L);
            return null;
        }).when(scheduleMapper).insertScheduleItem(any(TripScheduleItem.class));
        when(suggestionMapper.markApplied(11L, 99L)).thenReturn(1);

        var response = service.applySuggestion(1L, 11L, 7L);

        assertThat(response.status()).isEqualTo("APPLIED");
        assertThat(response.appliedScheduleItemId()).isEqualTo(99L);
        ArgumentCaptor<TripScheduleItem> captor = ArgumentCaptor.forClass(TripScheduleItem.class);
        verify(scheduleMapper).insertScheduleItem(captor.capture());
        assertThat(captor.getValue().getPlaceId()).isNull();
        assertThat(captor.getValue().getTitle()).isEqualTo("해운대 방문");
    }

    @Test
    void rejectsPendingSuggestion() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        TripScheduleMapper scheduleMapper = mock(TripScheduleMapper.class);
        AiSuggestionService service = new AiSuggestionService(suggestionMapper, scheduleMapper);

        when(scheduleMapper.existsEditableTrip(1L, 7L)).thenReturn(true);
        when(suggestionMapper.findSuggestionForUpdate(1L, 11L)).thenReturn(suggestion(11L, "PENDING"));
        when(suggestionMapper.markRejected(11L)).thenReturn(1);

        var response = service.rejectSuggestion(1L, 11L, 7L);

        assertThat(response.status()).isEqualTo("REJECTED");
        verify(suggestionMapper).markRejected(11L);
    }

    @Test
    void bulkAppliesOnlyPendingSuggestionsFromRun() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        TripScheduleMapper scheduleMapper = mock(TripScheduleMapper.class);
        AiSuggestionService service = new AiSuggestionService(suggestionMapper, scheduleMapper);

        when(scheduleMapper.existsEditableTrip(1L, 7L)).thenReturn(true);
        when(suggestionMapper.findPendingSuggestionsForRunForUpdate(1L, 5L))
                .thenReturn(List.of(suggestion(11L, "PENDING"), suggestion(12L, "PENDING")));
        doAnswer(invocation -> {
            TripScheduleItem item = invocation.getArgument(0);
            item.setScheduleItemId(item.getTitle().contains("해운대") ? 91L : 92L);
            return null;
        }).when(scheduleMapper).insertScheduleItem(any(TripScheduleItem.class));
        when(suggestionMapper.markApplied(any(Long.class), any(Long.class))).thenReturn(1);

        var response = service.applyRun(1L, 5L, 7L);

        assertThat(response).hasSize(2);
        assertThat(response).extracting(item -> item.status()).containsOnly("APPLIED");
    }

    @Test
    void refusesToApplyNonPendingSuggestion() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        TripScheduleMapper scheduleMapper = mock(TripScheduleMapper.class);
        AiSuggestionService service = new AiSuggestionService(suggestionMapper, scheduleMapper);

        when(scheduleMapper.existsEditableTrip(1L, 7L)).thenReturn(true);
        when(suggestionMapper.findSuggestionForUpdate(1L, 11L)).thenReturn(suggestion(11L, "REJECTED"));

        assertThatThrownBy(() -> service.applySuggestion(1L, 11L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private AiSuggestion suggestion(Long id, String status) {
        return new AiSuggestion(
                id, 5L, 1L, null, "해운대해수욕장", "부산 해운대구",
                "SCHEDULE", "해운대 방문", "오전 관광", "채팅 합의",
                LocalDate.of(2026, 7, 1), LocalTime.of(10, 0), LocalTime.of(12, 0),
                1, id.intValue(), status, null, null, null
        );
    }
}
