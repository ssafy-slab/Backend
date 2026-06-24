package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftItem;
import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftResponse;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiScheduleSlotValidatorTest {

    private final AiScheduleSlotValidator validator = new AiScheduleSlotValidator();

    @Test
    void defaultsMissingEndTimeToOneHour() {
        var result = validator.normalizeAndValidate(
                success(item(LocalDate.of(2026, 7, 1), LocalTime.of(9, 0), null, "카페")),
                trip(), List.of()
        );

        assertThat(result.schedules().get(0).endTime()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void rejectsOverlapWithExistingSchedule() {
        assertThatThrownBy(() -> validator.normalizeAndValidate(
                success(item(LocalDate.of(2026, 7, 1), LocalTime.of(9, 0), LocalTime.of(10, 0), "카페")),
                trip(),
                List.of(existing(LocalTime.of(9, 30), LocalTime.of(10, 30)))
        )).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void rejectsOverlapBetweenGeneratedSuggestions() {
        assertThatThrownBy(() -> validator.normalizeAndValidate(
                new AiScheduleDraftResponse("일정", List.of(), List.of(
                        item(LocalDate.of(2026, 7, 1), LocalTime.of(9, 0), LocalTime.of(10, 0), "카페"),
                        item(LocalDate.of(2026, 7, 1), LocalTime.of(9, 30), LocalTime.of(10, 30), "공원")
                )),
                trip(), List.of()
        )).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void rejectsScheduleStartingBeforeSeven() {
        assertThatThrownBy(() -> validator.normalizeAndValidate(
                success(item(LocalDate.of(2026, 7, 1), LocalTime.of(6, 30), null, "아침 산책")),
                trip(), List.of()
        )).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void acceptsSameDayScheduleEndingAtTwentyThreeFiftyNine() {
        var result = validator.normalizeAndValidate(
                success(item(
                        LocalDate.of(2026, 7, 1),
                        LocalTime.of(22, 0),
                        LocalTime.of(23, 59),
                        "야경"
                )),
                trip(), List.of()
        );

        assertThat(result.schedules()).hasSize(1);
    }

    @Test
    void acceptsFinalDayOvernightScheduleEndingAtSix() {
        var result = validator.normalizeAndValidate(
                success(item(
                        LocalDate.of(2026, 7, 3),
                        LocalTime.of(23, 0),
                        LocalTime.of(6, 0),
                        "새벽 산책"
                )),
                trip(), List.of()
        );

        assertThat(result.schedules()).hasSize(1);
    }

    @Test
    void acceptsEarlyMorningScheduleOnDayAfterTripEnds() {
        var result = validator.normalizeAndValidate(
                success(item(
                        LocalDate.of(2026, 7, 4),
                        LocalTime.MIDNIGHT,
                        LocalTime.of(2, 0),
                        "새벽 모임"
                )),
                trip(), List.of()
        );

        assertThat(result.schedules()).hasSize(1);
    }

    @Test
    void rejectsScheduleOnDayAfterTripEndingAfterSix() {
        assertThatThrownBy(() -> validator.normalizeAndValidate(
                success(item(
                        LocalDate.of(2026, 7, 4),
                        LocalTime.of(5, 0),
                        LocalTime.of(6, 1),
                        "늦은 새벽 일정"
                )),
                trip(), List.of()
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("outside the trip range");
    }

    @Test
    void rejectsFinalDayOvernightScheduleEndingAfterSix() {
        assertThatThrownBy(() -> validator.normalizeAndValidate(
                success(item(
                        LocalDate.of(2026, 7, 3),
                        LocalTime.of(23, 0),
                        LocalTime.of(6, 1),
                        "새벽 산책"
                )),
                trip(), List.of()
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("outside the trip range");
    }

    @Test
    void acceptsScheduleLastingExactlyTwelveHours() {
        var result = validator.normalizeAndValidate(
                success(item(
                        LocalDate.of(2026, 7, 1),
                        LocalTime.of(18, 0),
                        LocalTime.of(6, 0),
                        "야간 일정"
                )),
                trip(), List.of()
        );

        assertThat(result.schedules()).hasSize(1);
    }

    @Test
    void rejectsScheduleLongerThanTwelveHours() {
        assertThatThrownBy(() -> validator.normalizeAndValidate(
                success(item(
                        LocalDate.of(2026, 7, 1),
                        LocalTime.of(18, 0),
                        LocalTime.of(6, 1),
                        "너무 긴 일정"
                )),
                trip(), List.of()
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("longer than 12 hours");
    }

    @Test
    void rejectsOverlapWithExistingOvernightSchedule() {
        assertThatThrownBy(() -> validator.normalizeAndValidate(
                success(item(
                        LocalDate.of(2026, 7, 2),
                        LocalTime.of(7, 0),
                        LocalTime.of(9, 0),
                        "아침 일정"
                )),
                trip(),
                List.of(existing(
                        LocalDate.of(2026, 7, 1),
                        LocalTime.of(20, 0),
                        LocalTime.of(8, 0)
                ))
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("overlapping an existing schedule");
    }

    @Test
    void rejectsOverlapBetweenGeneratedOvernightSuggestions() {
        assertThatThrownBy(() -> validator.normalizeAndValidate(
                new AiScheduleDraftResponse("일정", List.of(), List.of(
                        item(
                                LocalDate.of(2026, 7, 1),
                                LocalTime.of(20, 0),
                                LocalTime.of(8, 0),
                                "야간 일정"
                        ),
                        item(
                                LocalDate.of(2026, 7, 2),
                                LocalTime.of(7, 0),
                                LocalTime.of(9, 0),
                                "아침 일정"
                        )
                )),
                trip(), List.of()
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("overlapping schedule suggestions");
    }

    private AiScheduleDraftResponse success(AiScheduleDraftItem item) {
        return new AiScheduleDraftResponse("일정", List.of(), List.of(item));
    }

    private AiScheduleDraftItem item(LocalDate date, LocalTime start, LocalTime end, String title) {
        return new AiScheduleDraftItem(null, null, date, start, end, title, null, 1, 1);
    }

    private TripScheduleItem existing(LocalTime start, LocalTime end) {
        return existing(LocalDate.of(2026, 7, 1), start, end);
    }

    private TripScheduleItem existing(LocalDate date, LocalTime start, LocalTime end) {
        return new TripScheduleItem(
                1L, 1L, null, 7L, 1, date, start, end,
                "기존 일정", null, 1, null, null
        );
    }

    private TripResponse trip() {
        return new TripResponse(
                1L, 7L, "부산 여행", null, "TEAM",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3),
                "PLANNING", LocalDateTime.now(), LocalDateTime.now()
        );
    }
}
