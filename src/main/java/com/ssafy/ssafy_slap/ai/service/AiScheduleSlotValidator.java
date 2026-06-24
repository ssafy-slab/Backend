package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftItem;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftResponse;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class AiScheduleSlotValidator {

    private static final LocalTime DAY_START = LocalTime.of(7, 0);
    private static final LocalTime FINAL_DAY_CUTOFF = LocalTime.of(6, 0);
    private static final Duration MAX_DURATION = Duration.ofHours(12);

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
        LocalDateTime start = startDateTime(item);
        LocalDateTime end = endDateTime(item);
        boolean finalMorning = isFinalMorning(item.scheduleDate(), trip.endDate());
        if (item.startTime().isBefore(DAY_START) && !finalMorning) {
            throw invalid("AI returned a schedule outside available hours");
        }
        if (Duration.between(start, end).compareTo(MAX_DURATION) > 0) {
            throw invalid("AI returned a schedule longer than 12 hours");
        }
        if (outsideTripDateRange(item.scheduleDate(), trip.startDate(), trip.endDate())) {
            throw invalid("AI returned a schedule date outside the trip range");
        }
        if (trip.endDate() != null) {
            LocalDateTime latestEnd = LocalDateTime.of(
                    trip.endDate().plusDays(1),
                    FINAL_DAY_CUTOFF
            );
            if (end.isAfter(latestEnd)) {
                throw invalid("AI returned a schedule outside the trip range");
            }
        }
    }

    private void validateNoExistingOverlap(
            AiScheduleDraftItem candidate,
            List<TripScheduleItem> existingSchedules
    ) {
        if (existingSchedules == null) {
            return;
        }
        TimeRange candidateRange = timeRange(candidate);
        for (TripScheduleItem existing : existingSchedules) {
            if (existing == null || existing.getScheduleDate() == null || existing.getStartTime() == null) {
                continue;
            }
            TimeRange existingRange = timeRange(existing);
            if (overlaps(candidateRange, existingRange)) {
                throw invalid("AI returned a schedule overlapping an existing schedule");
            }
        }
    }

    private void validateNoGeneratedOverlap(
            AiScheduleDraftItem candidate,
            List<AiScheduleDraftItem> generated
    ) {
        TimeRange candidateRange = timeRange(candidate);
        for (AiScheduleDraftItem existing : generated) {
            if (overlaps(candidateRange, timeRange(existing))) {
                throw invalid("AI returned overlapping schedule suggestions");
            }
        }
    }

    private boolean overlaps(TimeRange range, TimeRange other) {
        return range.start().isBefore(other.end()) && other.start().isBefore(range.end());
    }

    private TimeRange timeRange(AiScheduleDraftItem item) {
        return new TimeRange(startDateTime(item), endDateTime(item));
    }

    private TimeRange timeRange(TripScheduleItem item) {
        LocalDateTime start = LocalDateTime.of(item.getScheduleDate(), item.getStartTime());
        if (item.getEndTime() == null) {
            return new TimeRange(start, start.plusHours(1));
        }
        LocalDate endDate = item.getEndTime().isAfter(item.getStartTime())
                ? item.getScheduleDate()
                : item.getScheduleDate().plusDays(1);
        return new TimeRange(start, LocalDateTime.of(endDate, item.getEndTime()));
    }

    private LocalDateTime startDateTime(AiScheduleDraftItem item) {
        return LocalDateTime.of(item.scheduleDate(), item.startTime());
    }

    private LocalDateTime endDateTime(AiScheduleDraftItem item) {
        LocalDate endDate = item.endTime().isAfter(item.startTime())
                ? item.scheduleDate()
                : item.scheduleDate().plusDays(1);
        return LocalDateTime.of(endDate, item.endTime());
    }

    private boolean outsideTripDateRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return startDate != null && date.isBefore(startDate)
                || endDate != null && date.isAfter(endDate.plusDays(1));
    }

    private boolean isFinalMorning(LocalDate scheduleDate, LocalDate tripEndDate) {
        return tripEndDate != null && scheduleDate.equals(tripEndDate.plusDays(1));
    }

    private ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, message);
    }

    private record TimeRange(LocalDateTime start, LocalDateTime end) {
    }
}
