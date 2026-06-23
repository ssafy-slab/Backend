package com.ssafy.ssafy_slap.ai.service;

import com.ssafy.ssafy_slap.ai.dto.AiScheduleDraftResponse;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.trip.dto.TripResponse;

import java.util.List;

public interface AiScheduleClient {

    AiScheduleDraftResponse generate(
            TripResponse trip,
            List<ChatMessageResponse> messages,
            String additionalRequest
    );
}
