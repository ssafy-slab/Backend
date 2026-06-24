package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftResponse;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;
import com.ssafy.ssafy_slap.trip.domain.TripScheduleItem;

import java.util.List;

public interface AiScheduleClient {

    default AiScheduleDraftResponse generate(
            TripResponse trip,
            List<ChatMessageResponse> messages,
            String additionalRequest
    ) {
        return generate(trip, messages, List.of(), additionalRequest);
    }

    AiScheduleDraftResponse generate(
            TripResponse trip,
            List<ChatMessageResponse> messages,
            List<TripScheduleItem> existingSchedules,
            String additionalRequest
    );
}
