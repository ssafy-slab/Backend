package com.ssafy.ssafy_slap.trip.controller;

import com.ssafy.ssafy_slap.auth.service.AuthenticatedUser;
import com.ssafy.ssafy_slap.trip.dto.TripCreateRequest;
import com.ssafy.ssafy_slap.trip.dto.TripInviteCodeResponse;
import com.ssafy.ssafy_slap.trip.dto.TripJoinRequest;
import com.ssafy.ssafy_slap.trip.dto.TripListResponse;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import com.ssafy.ssafy_slap.trip.dto.TripScheduleCreateRequest;
import com.ssafy.ssafy_slap.trip.dto.TripScheduleResponse;
import com.ssafy.ssafy_slap.trip.dto.TripUpdateRequest;
import com.ssafy.ssafy_slap.trip.service.TripInviteService;
import com.ssafy.ssafy_slap.trip.service.TripScheduleService;
import com.ssafy.ssafy_slap.trip.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;
    private final TripInviteService tripInviteService;
    private final TripScheduleService tripScheduleService;

    public TripController(
            TripService tripService,
            TripInviteService tripInviteService,
            TripScheduleService tripScheduleService
    ) {
        this.tripService = tripService;
        this.tripInviteService = tripInviteService;
        this.tripScheduleService = tripScheduleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripResponse createTrip(
            Authentication authentication,
            @Valid @RequestBody TripCreateRequest request
    ) {
        return tripService.createTrip(currentUserId(authentication), request);
    }

    @GetMapping
    public List<TripListResponse> getMyTrips(Authentication authentication) {
        return tripService.findMyTrips(currentUserId(authentication));
    }

    @GetMapping("/{tripId}")
    public TripResponse getTrip(
            Authentication authentication,
            @PathVariable Long tripId
    ) {
        return tripService.findTrip(tripId, currentUserId(authentication));
    }

    @PutMapping("/{tripId}")
    public TripResponse updateTrip(
            Authentication authentication,
            @PathVariable Long tripId,
            @Valid @RequestBody TripUpdateRequest request
    ) {
        return tripService.updateTrip(tripId, currentUserId(authentication), request);
    }

    @DeleteMapping("/{tripId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrip(
            Authentication authentication,
            @PathVariable Long tripId
    ) {
        tripService.deleteTrip(tripId, currentUserId(authentication));
    }

    @PostMapping("/{tripId}/invite-code")
    public TripInviteCodeResponse createInviteCode(
            Authentication authentication,
            @PathVariable Long tripId
    ) {
        return tripInviteService.createInviteCode(tripId, currentUserId(authentication));
    }

    @PostMapping("/join")
    public TripResponse joinTrip(
            Authentication authentication,
            @Valid @RequestBody TripJoinRequest request
    ) {
        return tripInviteService.joinTrip(currentUserId(authentication), request);
    }

    @PostMapping("/{tripId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public TripScheduleResponse createScheduleItem(
            Authentication authentication,
            @PathVariable Long tripId,
            @Valid @RequestBody TripScheduleCreateRequest request
    ) {
        return tripScheduleService.createScheduleItem(tripId, currentUserId(authentication), request);
    }

    @DeleteMapping("/{tripId}/schedules/{scheduleItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScheduleItem(
            Authentication authentication,
            @PathVariable Long tripId,
            @PathVariable Long scheduleItemId
    ) {
        tripScheduleService.deleteScheduleItem(tripId, scheduleItemId, currentUserId(authentication));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user.userId();
    }
}
