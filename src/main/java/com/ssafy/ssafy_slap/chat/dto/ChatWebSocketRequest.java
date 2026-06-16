package com.ssafy.ssafy_slap.chat.dto;

public record ChatWebSocketRequest(
        String type,
        Long tripId,
        Long senderUserId,
        String content
) {

    public boolean isSubscribe() {
        return "SUBSCRIBE".equalsIgnoreCase(type);
    }

    public boolean isChat() {
        return type == null || type.isBlank() || "CHAT".equalsIgnoreCase(type);
    }
}
