package com.ssafy.ssafy_slap.chat.dto;

public record ChatMessageRequest(
        Long tripId,
        Long senderUserId,
        String content
) {
}
