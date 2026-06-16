package com.ssafy.ssafy_slap.chat.dto;

import com.ssafy.ssafy_slap.chat.domain.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long messageId,
        Long tripId,
        Long senderUserId,
        String senderNickname,
        String messageType,
        String content,
        LocalDateTime createdAt
) {

    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getMessageId(),
                message.getTripId(),
                message.getSenderUserId(),
                message.getSenderNickname(),
                message.getMessageType(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
