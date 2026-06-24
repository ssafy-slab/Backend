package com.ssafy.ssafy_slap.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GmsAiSchedulePromptTest {

    @Test
    void includesExistingSchedulesAndAvailabilityRules() {
        var client = new GmsAiScheduleClient(
                HttpClient.newHttpClient(),
                new ObjectMapper(),
                "key",
                "https://example.test",
                "model"
        );

        String prompt = client.userPrompt(trip(), List.of(message()), List.of(existing()), null);

        assertThat(prompt).contains("Existing schedules");
        assertThat(prompt).contains("2026-07-01 10:00-11:00");
        assertThat(prompt).contains("기존 일정");
    }

    @Test
    void systemPromptDescribesOvernightScheduleRules() throws Exception {
        Field field = GmsAiScheduleClient.class.getDeclaredField("SYSTEM_PROMPT");
        field.setAccessible(true);
        String systemPrompt = (String) field.get(null);

        assertThat(systemPrompt)
                .contains("scheduleDate is the start date")
                .contains("12 hours")
                .contains("06:00");
    }

    private TripResponse trip() {
        return new TripResponse(
                1L, 7L, "부산 여행", null, "TEAM",
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3),
                "PLANNING", LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private ChatMessageResponse message() {
        return new ChatMessageResponse(
                11L, 1L, 7L, "여행자", "TEXT", "카페 가자", LocalDateTime.now()
        );
    }

    private TripScheduleItem existing() {
        return new TripScheduleItem(
                91L, 1L, null, 7L, 1, LocalDate.of(2026, 7, 1),
                LocalTime.of(10, 0), LocalTime.of(11, 0),
                "기존 일정", null, 1, null, null
        );
    }
}
