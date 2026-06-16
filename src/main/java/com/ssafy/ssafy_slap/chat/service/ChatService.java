package com.ssafy.ssafy_slap.chat.service;

import com.ssafy.ssafy_slap.chat.domain.ChatMessage;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageRequest;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.chat.mapper.ChatMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final ChatMapper chatMapper;

    public ChatService(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
    }

    @Transactional
    public ChatMessageResponse createTextMessage(ChatMessageRequest request) {
        validateRequest(request);

        ChatMessage message = new ChatMessage(
                null,
                request.tripId(),
                request.senderUserId(),
                null,
                "TEXT",
                request.content().trim(),
                null
        );
        chatMapper.insertMessage(message);

        return ChatMessageResponse.from(chatMapper.findMessageById(message.getMessageId()));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> findRecentMessages(Long tripId, Integer limit) {
        if (tripId == null) {
            throw new IllegalArgumentException("tripId is required");
        }
        int normalizedLimit = normalizeLimit(limit);
        return chatMapper.findRecentMessages(tripId, normalizedLimit)
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    private void validateRequest(ChatMessageRequest request) {
        if (request.tripId() == null) {
            throw new IllegalArgumentException("tripId is required");
        }
        if (request.senderUserId() == null) {
            throw new IllegalArgumentException("senderUserId is required");
        }
        if (request.content() == null || request.content().trim().isEmpty()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (!chatMapper.existsTrip(request.tripId())) {
            throw new IllegalArgumentException("trip not found: " + request.tripId());
        }
        if (!chatMapper.existsUser(request.senderUserId())) {
            throw new IllegalArgumentException("user not found: " + request.senderUserId());
        }
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
