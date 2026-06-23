package com.ssafy.ssafy_slap.chat.service;

public record ChatMessageCreatedEvent(
        Long tripId,
        Long userId,
        Long messageId
) {
}
