package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.mapper.AiSuggestionMapper;
import com.ssafy.ssafy_slap.trip.mapper.TripScheduleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiSuggestionTeamRoutingTest {

    @Test
    void blocksDirectApplyForTeamTrip() {
        AiSuggestionMapper suggestionMapper = mock(AiSuggestionMapper.class);
        TripScheduleMapper scheduleMapper = mock(TripScheduleMapper.class);
        AiSuggestionService service = new AiSuggestionService(suggestionMapper, scheduleMapper);

        when(scheduleMapper.existsEditableTrip(1L, 7L)).thenReturn(true);
        when(suggestionMapper.findTripType(1L)).thenReturn("TEAM");

        assertThatThrownBy(() -> service.applySuggestion(1L, 11L, 7L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
