package com.ssafy.ssafy_slap.trip.service;

import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.dto.TripScheduleCreateRequest;
import com.ssafy.ssafy_slap.trip.dto.TripScheduleResponse;
import com.ssafy.ssafy_slap.trip.mapper.TripScheduleMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TripScheduleService {

    private final TripScheduleMapper tripScheduleMapper;

    public TripScheduleService(TripScheduleMapper tripScheduleMapper) {
        this.tripScheduleMapper = tripScheduleMapper;
    }

    @Transactional
    public TripScheduleResponse createScheduleItem(Long tripId, Long userId, TripScheduleCreateRequest request) {
        validateCreateRequest(tripId, userId, request);

        TripScheduleItem item = new TripScheduleItem(
                null,
                tripId,
                request.placeId(),
                userId,
                request.dayNo(),
                request.scheduleDate(),
                request.startTime(),
                request.endTime(),
                normalizeText(request.title()),
                normalizeText(request.memo()),
                request.sortOrder(),
                null,
                null
        );

        tripScheduleMapper.insertScheduleItem(item);
        return TripScheduleResponse.from(tripScheduleMapper.findScheduleItemById(item.getScheduleItemId()));
    }

    @Transactional
    public void deleteScheduleItem(Long tripId, Long scheduleItemId, Long userId) {
        validateTripAccess(tripId, userId);
        int deleted = tripScheduleMapper.deleteScheduleItem(tripId, scheduleItemId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule item not found");
        }
    }

    private void validateCreateRequest(Long tripId, Long userId, TripScheduleCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
        }
        if (request.placeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "placeId is required");
        }
        if (request.scheduleDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scheduleDate is required");
        }
        if (request.startTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime is required");
        }
        if (request.endTime() != null && request.endTime().isBefore(request.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime must not be before startTime");
        }
        validateTripAccess(tripId, userId);
        if (!tripScheduleMapper.existsPlace(request.placeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
        }
    }

    private void validateTripAccess(Long tripId, Long userId) {
        if (tripId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tripId is required");
        }
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        if (!tripScheduleMapper.existsEditableTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip is not editable");
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
