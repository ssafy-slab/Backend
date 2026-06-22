package com.ssafy.ssafy_slap.trip.service;

import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.dto.TripScheduleCreateRequest;
import com.ssafy.ssafy_slap.trip.mapper.TripScheduleMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripScheduleServiceTest {

    @Test
    void createsScheduleItemForAccessibleTripAndPlace() {
        TripScheduleMapper mapper = mock(TripScheduleMapper.class);
        TripScheduleService service = new TripScheduleService(mapper);
        TripScheduleCreateRequest request = new TripScheduleCreateRequest(
                100L,
                LocalDate.of(2026, 7, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                "  beach walk  ",
                "  bring water  ",
                1,
                2
        );

        when(mapper.existsEditableTrip(1L, 10L)).thenReturn(true);
        when(mapper.existsPlace(100L)).thenReturn(true);
        doAnswer(invocation -> {
            TripScheduleItem item = invocation.getArgument(0);
            item.setScheduleItemId(99L);
            return null;
        }).when(mapper).insertScheduleItem(org.mockito.ArgumentMatchers.any(TripScheduleItem.class));
        when(mapper.findScheduleItemById(99L)).thenReturn(new TripScheduleItem(
                99L,
                1L,
                100L,
                10L,
                1,
                LocalDate.of(2026, 7, 1),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                "beach walk",
                "bring water",
                2,
                LocalDateTime.of(2026, 6, 17, 10, 0),
                LocalDateTime.of(2026, 6, 17, 10, 0)
        ));

        var response = service.createScheduleItem(1L, 10L, request);

        assertThat(response.scheduleItemId()).isEqualTo(99L);
        assertThat(response.tripId()).isEqualTo(1L);
        assertThat(response.placeId()).isEqualTo(100L);
        assertThat(response.title()).isEqualTo("beach walk");
        assertThat(response.memo()).isEqualTo("bring water");

        ArgumentCaptor<TripScheduleItem> itemCaptor = ArgumentCaptor.forClass(TripScheduleItem.class);
        verify(mapper).insertScheduleItem(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getTripId()).isEqualTo(1L);
        assertThat(itemCaptor.getValue().getCreatedByUserId()).isEqualTo(10L);
        assertThat(itemCaptor.getValue().getPlaceId()).isEqualTo(100L);
    }

    @Test
    void rejectsScheduleItemWithoutPlace() {
        TripScheduleMapper mapper = mock(TripScheduleMapper.class);
        TripScheduleService service = new TripScheduleService(mapper);
        TripScheduleCreateRequest request = new TripScheduleCreateRequest(
                null,
                LocalDate.of(2026, 7, 1),
                LocalTime.of(10, 0),
                null,
                "beach walk",
                null,
                null,
                null
        );

        assertThatThrownBy(() -> service.createScheduleItem(1L, 10L, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deletesScheduleItemFromAccessibleTrip() {
        TripScheduleMapper mapper = mock(TripScheduleMapper.class);
        TripScheduleService service = new TripScheduleService(mapper);

        when(mapper.existsEditableTrip(1L, 10L)).thenReturn(true);
        when(mapper.deleteScheduleItem(1L, 99L)).thenReturn(1);

        service.deleteScheduleItem(1L, 99L, 10L);

        verify(mapper).deleteScheduleItem(1L, 99L);
    }

    @Test
    void rejectsDeleteWhenTripIsNotAccessible() {
        TripScheduleMapper mapper = mock(TripScheduleMapper.class);
        TripScheduleService service = new TripScheduleService(mapper);

        when(mapper.existsEditableTrip(1L, 10L)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteScheduleItem(1L, 99L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
