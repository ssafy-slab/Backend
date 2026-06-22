package com.ssafy.ssafy_slap.trip.service;

import com.ssafy.ssafy_slap.trip.domain.Trip;
import com.ssafy.ssafy_slap.trip.domain.TripMember;
import com.ssafy.ssafy_slap.trip.dto.TripCreateRequest;
import com.ssafy.ssafy_slap.trip.dto.TripListResponse;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import com.ssafy.ssafy_slap.trip.dto.TripUpdateRequest;
import com.ssafy.ssafy_slap.trip.mapper.TripMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TripService {

    private final TripMapper tripMapper;

    public TripService(TripMapper tripMapper) {
        this.tripMapper = tripMapper;
    }

    @Transactional
    public TripResponse createTrip(Long ownerUserId, TripCreateRequest request) {
        validateRequest(request);

        Trip trip = new Trip(
                null,
                ownerUserId,
                normalizeRequiredText(request.title(), "title is required"),
                normalizeText(request.description()),
                normalizeText(request.tripType()),
                request.startDate(),
                request.endDate(),
                "PLANNING",
                null,
                null
        );

        tripMapper.insertTrip(trip);
        tripMapper.insertTripMember(trip.getTripId(), ownerUserId, "OWNER", "ACCEPTED");
        return TripResponse.from(tripMapper.findTripById(trip.getTripId()));
    }

    @Transactional(readOnly = true)
    public List<TripListResponse> findMyTrips(Long userId) {
        validateUserId(userId);
        List<Trip> trips = tripMapper.findAccessibleTrips(userId);
        if (trips.isEmpty()) {
            return List.of();
        }
        List<Long> tripIds = trips.stream().map(Trip::getTripId).toList();
        Map<Long, List<TripMember>> membersByTripId = tripMapper.findMembersByTripIds(tripIds)
                .stream()
                .collect(Collectors.groupingBy(TripMember::getTripId));
        return trips
                .stream()
                .map(trip -> TripListResponse.from(trip, membersByTripId.getOrDefault(trip.getTripId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public TripResponse findTrip(Long tripId, Long userId) {
        validateTripId(tripId);
        validateUserId(userId);
        Trip trip = tripMapper.findAccessibleTripById(tripId, userId);
        if (trip == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found");
        }
        return TripResponse.from(trip);
    }

    @Transactional
    public TripResponse updateTrip(Long tripId, Long userId, TripUpdateRequest request) {
        validateTripId(tripId);
        validateUserId(userId);
        validateUpdateRequest(request);

        int updated = tripMapper.updateOwnedTrip(
                tripId,
                userId,
                normalizeRequiredText(request.title(), "title is required"),
                normalizeText(request.description()),
                normalizeText(request.tripType()),
                request.startDate(),
                request.endDate()
        );
        if (updated == 1) {
            return TripResponse.from(tripMapper.findTripById(tripId));
        }
        if (tripMapper.existsAccessibleTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only trip owner can update trip");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found");
    }

    @Transactional
    public void deleteTrip(Long tripId, Long userId) {
        validateTripId(tripId);
        validateUserId(userId);
        int deleted = tripMapper.deleteOwnedTrip(tripId, userId);
        if (deleted == 1) {
            return;
        }
        if (tripMapper.existsAccessibleTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only trip owner can delete trip");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found");
    }

    private void validateRequest(TripCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
        }
        normalizeRequiredText(request.title(), "title is required");
        if (request.startDate() != null && request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must not be before startDate");
        }
    }

    private void validateUpdateRequest(TripUpdateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
        }
        normalizeRequiredText(request.title(), "title is required");
        if (request.startDate() != null && request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must not be before startDate");
        }
    }

    private void validateTripId(Long tripId) {
        if (tripId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tripId is required");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
    }

    private String normalizeRequiredText(String text, String message) {
        String normalized = normalizeText(text);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
