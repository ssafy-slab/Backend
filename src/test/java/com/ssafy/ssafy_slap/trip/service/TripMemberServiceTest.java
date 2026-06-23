package com.ssafy.ssafy_slap.trip.service;

import com.ssafy.ssafy_slap.trip.domain.Trip;
import com.ssafy.ssafy_slap.trip.domain.TripMember;
import com.ssafy.ssafy_slap.trip.dto.TripMemberRoleUpdateRequest;
import com.ssafy.ssafy_slap.trip.mapper.TripMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripMemberServiceTest {

    @Test
    void findsMembersForAccessibleTrip() {
        TripMapper mapper = mock(TripMapper.class);
        TripMemberService service = new TripMemberService(mapper);
        when(mapper.existsAccessibleTrip(7L, 10L)).thenReturn(true);
        when(mapper.findMembersByTripId(7L)).thenReturn(List.of(
                new TripMember(1L, 7L, 10L, "owner", "OWNER", "ACCEPTED", LocalDateTime.of(2026, 6, 22, 10, 0)),
                new TripMember(2L, 7L, 20L, "member", "EDITOR", "ACCEPTED", LocalDateTime.of(2026, 6, 22, 11, 0))
        ));

        var response = service.findMembers(7L, 10L);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).memberRole()).isEqualTo("OWNER");
        assertThat(response.get(1).userId()).isEqualTo(20L);
    }

    @Test
    void leavesTripWhenCurrentUserIsMember() {
        TripMapper mapper = mock(TripMapper.class);
        TripMemberService service = new TripMemberService(mapper);
        when(mapper.findTripMember(7L, 20L)).thenReturn(
                new TripMember(2L, 7L, 20L, "member", "EDITOR", "ACCEPTED", LocalDateTime.of(2026, 6, 22, 11, 0))
        );
        when(mapper.deleteTripMember(7L, 20L)).thenReturn(1);

        service.leaveTrip(7L, 20L);

        verify(mapper).deleteTripMember(7L, 20L);
    }

    @Test
    void rejectsLeaveWhenCurrentUserIsOwner() {
        TripMapper mapper = mock(TripMapper.class);
        TripMemberService service = new TripMemberService(mapper);
        when(mapper.findTripMember(7L, 10L)).thenReturn(
                new TripMember(1L, 7L, 10L, "owner", "OWNER", "ACCEPTED", LocalDateTime.of(2026, 6, 22, 10, 0))
        );

        assertThatThrownBy(() -> service.leaveTrip(7L, 10L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updatesMemberRoleWhenCurrentUserIsOwner() {
        TripMapper mapper = mock(TripMapper.class);
        TripMemberService service = new TripMemberService(mapper);
        when(mapper.findTripById(7L)).thenReturn(teamTrip());
        when(mapper.findTripMember(7L, 20L)).thenReturn(
                new TripMember(2L, 7L, 20L, "member", "EDITOR", "ACCEPTED", LocalDateTime.of(2026, 6, 22, 11, 0))
        );
        when(mapper.updateTripMemberRole(7L, 20L, "VIEWER")).thenReturn(1);

        var response = service.updateMemberRole(7L, 20L, 10L, new TripMemberRoleUpdateRequest("viewer"));

        assertThat(response.userId()).isEqualTo(20L);
        assertThat(response.memberRole()).isEqualTo("VIEWER");
        verify(mapper).updateTripMemberRole(7L, 20L, "VIEWER");
    }

    @Test
    void rejectsRoleUpdateWhenCurrentUserIsNotOwner() {
        TripMapper mapper = mock(TripMapper.class);
        TripMemberService service = new TripMemberService(mapper);
        when(mapper.findTripById(7L)).thenReturn(teamTrip());

        assertThatThrownBy(() -> service.updateMemberRole(7L, 20L, 99L, new TripMemberRoleUpdateRequest("VIEWER")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void rejectsRoleUpdateWhenCurrentUserIsEditor() {
        TripMapper mapper = mock(TripMapper.class);
        TripMemberService service = new TripMemberService(mapper);
        when(mapper.findTripById(7L)).thenReturn(teamTrip());
        when(mapper.findTripMember(7L, 30L)).thenReturn(
                new TripMember(3L, 7L, 30L, "viewer", "VIEWER", "ACCEPTED", LocalDateTime.of(2026, 6, 22, 12, 0))
        );

        assertThatThrownBy(() -> service.updateMemberRole(7L, 30L, 20L, new TripMemberRoleUpdateRequest("EDITOR")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void rejectsRoleUpdateToOwner() {
        TripMapper mapper = mock(TripMapper.class);
        TripMemberService service = new TripMemberService(mapper);
        when(mapper.findTripById(7L)).thenReturn(teamTrip());

        assertThatThrownBy(() -> service.updateMemberRole(7L, 20L, 10L, new TripMemberRoleUpdateRequest("OWNER")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(exception -> ((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
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
                LocalDateTime.of(2026, 6, 22, 10, 0),
                LocalDateTime.of(2026, 6, 22, 10, 0)
        );
    }
}
