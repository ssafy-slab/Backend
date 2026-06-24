package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftItem;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftResponse;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AiScheduleSlotValidator {

    private static final LocalTime DAY_START = LocalTime.of(7, 0);
    private static final LocalTime DAY_END = LocalTime.of(23, 0);

    public AiScheduleDraftResponse normalizeAndValidate(
            AiScheduleDraftResponse draft,
            TripResponse trip,
            List<TripScheduleItem> existingSchedules
    ) {
        if (draft == null || draft.isNoResult()) {
            return draft;
        }
        if (draft.schedules() == null || draft.schedules().isEmpty()) {
            throw invalid("AI returned no schedule items");
        }

        List<AiScheduleDraftItem> normalized = new ArrayList<>();
        for (AiScheduleDraftItem item : draft.schedules()) {
            AiScheduleDraftItem normalizedItem = normalize(item);
            validateBounds(normalizedItem, trip);
            validateNoExistingOverlap(normalizedItem, existingSchedules);
            validateNoGeneratedOverlap(normalizedItem, normalized);
            normalized.add(normalizedItem);
        }
        return new AiScheduleDraftResponse(
                draft.summary(),
                draft.warnings(),
                List.copyOf(normalized),
                draft.resultStatus(),
                draft.reasonCode(),
                draft.message()
        );
    }

    private AiScheduleDraftItem normalize(AiScheduleDraftItem item) {
        if (item == null || item.scheduleDate() == null || item.startTime() == null
                || item.title() == null || item.title().isBlank()) {
            throw invalid("AI returned an incomplete schedule item");
        }
        LocalTime endTime = item.endTime() == null
                ? item.startTime().plusHours(1)
                : item.endTime();
        return new AiScheduleDraftItem(
                item.placeName(), item.regionHint(), item.scheduleDate(), item.startTime(),
                endTime, item.title().trim(), item.memo(), item.dayNo(), item.sortOrder()
        );
    }

    private void validateBounds(AiScheduleDraftItem item, TripResponse trip) {
        if (!item.startTime().isBefore(item.endTime())) {
            throw invalid("AI returned an invalid schedule time range");
        }
        if (item.startTime().isBefore(DAY_START) || item.endTime().isAfter(DAY_END)) {
            throw invalid("AI returned a schedule outside available hours");
        }
        if (outsideTripDateRange(item.scheduleDate(), trip.startDate(), trip.endDate())) {
            throw invalid("AI returned a schedule date outside the trip range");
        }
    }

    private void validateNoExistingOverlap(
            AiScheduleDraftItem candidate,
            List<TripScheduleItem> existingSchedules
    ) {
        if (existingSchedules == null) {
            return;
        }
        for (TripScheduleItem existing : existingSchedules) {
            if (existing == null || existing.getScheduleDate() == null || existing.getStartTime() == null
                    || !candidate.scheduleDate().equals(existing.getScheduleDate())) {
                continue;
            }
            LocalTime existingEnd = existing.getEndTime() == null
                    ? existing.getStartTime().plusHours(1)
                    : existing.getEndTime();
            if (overlaps(candidate.startTime(), candidate.endTime(), existing.getStartTime(), existingEnd)) {
                throw invalid("AI returned a schedule overlapping an existing schedule");
            }
        }
    }

    private void validateNoGeneratedOverlap(
            AiScheduleDraftItem candidate,
            List<AiScheduleDraftItem> generated
    ) {
        for (AiScheduleDraftItem existing : generated) {
            if (candidate.scheduleDate().equals(existing.scheduleDate())
                    && overlaps(candidate.startTime(), candidate.endTime(),
                    existing.startTime(), existing.endTime())) {
                throw invalid("AI returned overlapping schedule suggestions");
            }
        }
    }

    private boolean overlaps(LocalTime start, LocalTime end, LocalTime otherStart, LocalTime otherEnd) {
        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }

    private boolean outsideTripDateRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return startDate != null && date.isBefore(startDate)
                || endDate != null && date.isAfter(endDate);
    }

    private ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
    }
}
