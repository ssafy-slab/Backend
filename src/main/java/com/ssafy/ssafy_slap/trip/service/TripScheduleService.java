package com.ssafy.ssafy_slap.trip.service;

import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.dto.TripScheduleCreateRequest;
import com.ssafy.ssafy_slap.trip.dto.TripScheduleResponse;
import com.ssafy.ssafy_slap.trip.dto.TripScheduleUpdateRequest;
import com.ssafy.ssafy_slap.trip.mapper.TripScheduleMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<TripScheduleResponse> findScheduleItems(Long tripId, Long userId) {
        validateTripReadAccess(tripId, userId);
        return tripScheduleMapper.findScheduleItemsByTripId(tripId).stream()
                .map(TripScheduleResponse::from)
                .toList();
    }

    @Transactional
    public TripScheduleResponse updateScheduleItem(
            Long tripId,
            Long scheduleItemId,
            Long userId,
            TripScheduleUpdateRequest request
    ) {
        validateUpdateRequest(tripId, scheduleItemId, userId, request);

        TripScheduleItem item = new TripScheduleItem(
                scheduleItemId,
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

        int updated = tripScheduleMapper.updateScheduleItem(item);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule item not found");
        }
        return TripScheduleResponse.from(tripScheduleMapper.findScheduleItemById(scheduleItemId));
    }

    @Transactional
    public void deleteScheduleItem(Long tripId, Long scheduleItemId, Long userId) {
        validateTripEditAccess(tripId, userId);
        int deleted = tripScheduleMapper.deleteScheduleItem(tripId, scheduleItemId);
        if (deleted == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule item not found");
        }
    }

    private void validateCreateRequest(Long tripId, Long userId, TripScheduleCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
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
        validateTripEditAccess(tripId, userId);
        if (request.placeId() != null && !tripScheduleMapper.existsPlace(request.placeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
        }
    }

    private void validateUpdateRequest(
            Long tripId,
            Long scheduleItemId,
            Long userId,
            TripScheduleUpdateRequest request
    ) {
        if (scheduleItemId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scheduleItemId is required");
        }
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "request body is required");
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
        validateTripEditAccess(tripId, userId);
        if (request.placeId() != null && !tripScheduleMapper.existsPlace(request.placeId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
        }
    }

    private void validateTripReadAccess(Long tripId, Long userId) {
        if (tripId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tripId is required");
        }
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        if (!tripScheduleMapper.existsAccessibleTrip(tripId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Trip is not accessible");
        }
    }

    private void validateTripEditAccess(Long tripId, Long userId) {
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
