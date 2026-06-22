package com.ssafy.ssafy_slap.trip.service;

import com.ssafy.ssafy_slap.trip.domain.Trip;
import com.ssafy.ssafy_slap.trip.domain.TripMember;
import com.ssafy.ssafy_slap.trip.dto.TripCreateRequest;
import com.ssafy.ssafy_slap.trip.dto.TripUpdateRequest;
import com.ssafy.ssafy_slap.trip.mapper.TripMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripServiceTest {

    @Test
    void createsTripPlanAndAddsOwnerAsMember() {
        TripMapper mapper = mock(TripMapper.class);
        TripService service = new TripService(mapper);
        TripCreateRequest request = new TripCreateRequest(
                "  Busan trip  ",
                "  summer plan  ",
                "TEAM",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3)
        );

        doAnswer(invocation -> {
            Trip trip = invocation.getArgument(0);
            trip.setTripId(7L);
            return null;
        }).when(mapper).insertTrip(org.mockito.ArgumentMatchers.any(Trip.class));
        when(mapper.findTripById(7L)).thenReturn(new Trip(
                7L,
                10L,
                "Busan trip",
                "summer plan",
                "TEAM",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                "PLANNING",
                LocalDateTime.of(2026, 6, 17, 10, 0),
                LocalDateTime.of(2026, 6, 17, 10, 0)
        ));

        var response = service.createTrip(10L, request);

        assertThat(response.tripId()).isEqualTo(7L);
        assertThat(response.ownerUserId()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("Busan trip");

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(mapper).insertTrip(tripCaptor.capture());
        assertThat(tripCaptor.getValue().getOwnerUserId()).isEqualTo(10L);
        assertThat(tripCaptor.getValue().getTitle()).isEqualTo("Busan trip");
        verify(mapper).insertTripMember(7L, 10L, "OWNER", "ACCEPTED");
    }

    @Test
    void rejectsTripWhenEndDateIsBeforeStartDate() {
        TripMapper mapper = mock(TripMapper.class);
        TripService service = new TripService(mapper);
        TripCreateRequest request = new TripCreateRequest(
                "Busan trip",
                null,
                "TEAM",
                LocalDate.of(2026, 7, 3),
                LocalDate.of(2026, 7, 1)
        );

        assertThatThrownBy(() -> service.createTrip(10L, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void findsAccessibleTripsForUser() {
        TripMapper mapper = mock(TripMapper.class);
        TripService service = new TripService(mapper);
        when(mapper.findAccessibleTrips(10L)).thenReturn(List.of(new Trip(
                7L,
                10L,
                "Busan trip",
                "summer plan",
                "TEAM",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                "PLANNING",
                LocalDateTime.of(2026, 6, 17, 10, 0),
                LocalDateTime.of(2026, 6, 17, 10, 0)
        )));
        when(mapper.findMembersByTripIds(List.of(7L))).thenReturn(List.of(
                new TripMember(1L, 7L, 10L, "owner", "OWNER", "ACCEPTED", LocalDateTime.of(2026, 6, 17, 10, 0)),
                new TripMember(2L, 7L, 99L, "member", "EDITOR", "ACCEPTED", LocalDateTime.of(2026, 6, 17, 11, 0))
        ));

        var response = service.findMyTrips(10L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).tripId()).isEqualTo(7L);
        assertThat(response.get(0).title()).isEqualTo("Busan trip");
        assertThat(response.get(0).members()).hasSize(2);
        assertThat(response.get(0).members().get(0).userId()).isEqualTo(10L);
        assertThat(response.get(0).members().get(0).memberRole()).isEqualTo("OWNER");
    }

    @Test
    void findsAccessibleTripDetail() {
        TripMapper mapper = mock(TripMapper.class);
        TripService service = new TripService(mapper);
        when(mapper.findAccessibleTripById(7L, 10L)).thenReturn(new Trip(
                7L,
                10L,
                "Busan trip",
                "summer plan",
                "TEAM",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                "PLANNING",
                LocalDateTime.of(2026, 6, 17, 10, 0),
                LocalDateTime.of(2026, 6, 17, 10, 0)
        ));

        var response = service.findTrip(7L, 10L);

        assertThat(response.tripId()).isEqualTo(7L);
        assertThat(response.ownerUserId()).isEqualTo(10L);
    }

    @Test
    void rejectsTripDetailWhenNotAccessible() {
        TripMapper mapper = mock(TripMapper.class);
        TripService service = new TripService(mapper);

        assertThatThrownBy(() -> service.findTrip(7L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletesOwnedTrip() {
        TripMapper mapper = mock(TripMapper.class);
        TripService service = new TripService(mapper);
        when(mapper.deleteOwnedTrip(7L, 10L)).thenReturn(1);

        service.deleteTrip(7L, 10L);

        verify(mapper).deleteOwnedTrip(7L, 10L);
    }

    @Test
    void rejectsDeleteWhenUserIsNotOwner() {
        TripMapper mapper = mock(TripMapper.class);
        TripService service = new TripService(mapper);
        when(mapper.existsAccessibleTrip(7L, 10L)).thenReturn(true);
        when(mapper.deleteOwnedTrip(7L, 10L)).thenReturn(0);

        assertThatThrownBy(() -> service.deleteTrip(7L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void updatesOwnedTrip() {
        TripMapper mapper = mock(TripMapper.class);
        TripService service = new TripService(mapper);
        TripUpdateRequest request = new TripUpdateRequest(
                "  Seoul trip  ",
                "  autumn plan  ",
                "PERSONAL",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5)
        );
        when(mapper.updateOwnedTrip(
                7L,
                10L,
                "Seoul trip",
                "autumn plan",
                "PERSONAL",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5)
        )).thenReturn(1);
        when(mapper.findTripById(7L)).thenReturn(new Trip(
                7L,
                10L,
                "Seoul trip",
                "autumn plan",
                "PERSONAL",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5),
                "PLANNING",
                LocalDateTime.of(2026, 6, 17, 10, 0),
                LocalDateTime.of(2026, 6, 17, 11, 0)
        ));

        var response = service.updateTrip(7L, 10L, request);

        assertThat(response.title()).isEqualTo("Seoul trip");
        assertThat(response.description()).isEqualTo("autumn plan");
        assertThat(response.tripType()).isEqualTo("PERSONAL");
        verify(mapper).updateOwnedTrip(
                7L,
                10L,
                "Seoul trip",
                "autumn plan",
                "PERSONAL",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5)
        );
    }

    @Test
    void rejectsUpdateWhenUserIsNotOwner() {
        TripMapper mapper = mock(TripMapper.class);
        TripService service = new TripService(mapper);
        TripUpdateRequest request = new TripUpdateRequest(
                "Seoul trip",
                null,
                "PERSONAL",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5)
        );
        when(mapper.updateOwnedTrip(
                7L,
                10L,
                "Seoul trip",
                null,
                "PERSONAL",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 5)
        )).thenReturn(0);
        when(mapper.existsAccessibleTrip(7L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateTrip(7L, 10L, request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }
}
