package com.ssafy.ssafy_slap.trip.service;

import com.ssafy.ssafy_slap.trip.domain.Trip;
import com.ssafy.ssafy_slap.trip.domain.TripInviteCode;
import com.ssafy.ssafy_slap.trip.dto.TripJoinRequest;
import com.ssafy.ssafy_slap.trip.mapper.TripInviteMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripInviteServiceTest {

    @Test
    void createsInviteCodeForOwnedTeamTrip() {
        TripInviteMapper mapper = mock(TripInviteMapper.class);
        TripInviteService service = new TripInviteService(mapper);

        when(mapper.findTripById(7L)).thenReturn(teamTrip());
        when(mapper.findActiveInviteCodeByTripId(7L)).thenReturn(null);
        when(mapper.existsInviteCode(any())).thenReturn(false);
        doAnswer(invocation -> {
            TripInviteCode inviteCode = invocation.getArgument(0);
            inviteCode.setTripInviteCodeId(1L);
            return null;
        }).when(mapper).insertInviteCode(any(TripInviteCode.class));

        var response = service.createInviteCode(7L, 10L);

        assertThat(response.tripId()).isEqualTo(7L);
        assertThat(response.inviteCode()).hasSize(8);
        verify(mapper).insertInviteCode(any(TripInviteCode.class));
    }

    @Test
    void reusesExistingActiveInviteCode() {
        TripInviteMapper mapper = mock(TripInviteMapper.class);
        TripInviteService service = new TripInviteService(mapper);

        when(mapper.findTripById(7L)).thenReturn(teamTrip());
        when(mapper.findActiveInviteCodeByTripId(7L)).thenReturn(new TripInviteCode(
                1L,
                7L,
                "ABCD1234",
                10L,
                "ACTIVE",
                LocalDateTime.of(2026, 6, 17, 10, 0)
        ));

        var response = service.createInviteCode(7L, 10L);

        assertThat(response.inviteCode()).isEqualTo("ABCD1234");
    }

    @Test
    void rejectsInviteCodeForNonOwner() {
        TripInviteMapper mapper = mock(TripInviteMapper.class);
        TripInviteService service = new TripInviteService(mapper);

        when(mapper.findTripById(7L)).thenReturn(teamTrip());

        assertThatThrownBy(() -> service.createInviteCode(7L, 99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void rejectsInviteCodeForPersonalTrip() {
        TripInviteMapper mapper = mock(TripInviteMapper.class);
        TripInviteService service = new TripInviteService(mapper);

        when(mapper.findTripById(7L)).thenReturn(new Trip(
                7L,
                10L,
                "Solo",
                null,
                "PERSONAL",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                "PLANNING",
                LocalDateTime.of(2026, 6, 17, 10, 0),
                LocalDateTime.of(2026, 6, 17, 10, 0)
        ));

        assertThatThrownBy(() -> service.createInviteCode(7L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void joinsTeamTripWithInviteCode() {
        TripInviteMapper mapper = mock(TripInviteMapper.class);
        TripInviteService service = new TripInviteService(mapper);

        when(mapper.findTripByInviteCode("ABCD1234")).thenReturn(teamTrip());
        when(mapper.existsTripMember(7L, 99L)).thenReturn(false);

        var response = service.joinTrip(99L, new TripJoinRequest(" abcd1234 "));

        assertThat(response.tripId()).isEqualTo(7L);
        verify(mapper).insertTripMember(7L, 99L, "MEMBER", "ACCEPTED");
    }

    @Test
    void rejectsUnknownInviteCode() {
        TripInviteMapper mapper = mock(TripInviteMapper.class);
        TripInviteService service = new TripInviteService(mapper);

        assertThatThrownBy(() -> service.joinTrip(99L, new TripJoinRequest("NOPE1234")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Trip teamTrip() {
        return new Trip(
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
        );
    }
}
