package com.ssafy.ssafy_slap.chat.dto;

public record ChatWebSocketResponse(
        String type,
        Long tripId,
        ChatMessageResponse message,
        String error
) {

    public static ChatWebSocketResponse subscribed(Long tripId) {
        return new ChatWebSocketResponse("SUBSCRIBED", tripId, null, null);
    }

    public static ChatWebSocketResponse message(ChatMessageResponse message) {
        return new ChatWebSocketResponse("MESSAGE", message.tripId(), message, null);
    }

    public static ChatWebSocketResponse error(String error) {
        return new ChatWebSocketResponse("ERROR", null, null, error);
    }
}
