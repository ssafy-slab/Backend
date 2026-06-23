package com.ssafy.ssafy_slap.chat.service;

import com.ssafy.ssafy_slap.chat.domain.ChatMessage;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageRequest;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageResponse;
import com.ssafy.ssafy_slap.chat.mapper.ChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private final ChatMapper chatMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ChatService(ChatMapper chatMapper, ApplicationEventPublisher eventPublisher) {
        this.chatMapper = chatMapper;
        this.eventPublisher = eventPublisher;
    }

    ChatService(ChatMapper chatMapper) {
        this(chatMapper, event -> {
        });
    }

    @Transactional
    public ChatMessageResponse createTextMessage(Long authenticatedUserId, ChatMessageRequest request) {
        validateRequest(authenticatedUserId, request);

        ChatMessage message = new ChatMessage(
                null,
                request.tripId(),
                authenticatedUserId,
                null,
                "TEXT",
                request.content().trim(),
                null
        );
        chatMapper.insertMessage(message);

        ChatMessageResponse response = ChatMessageResponse.from(chatMapper.findMessageById(message.getMessageId()));
        eventPublisher.publishEvent(new ChatMessageCreatedEvent(
                response.tripId(),
                authenticatedUserId,
                response.messageId()
        ));
        return response;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> findRecentMessages(Long authenticatedUserId, Long tripId, Integer limit) {
        if (tripId == null) {
            throw new IllegalArgumentException("tripId is required");
        }
        validateTripAccess(authenticatedUserId, tripId);
        int normalizedLimit = normalizeLimit(limit);
        return chatMapper.findRecentMessages(tripId, normalizedLimit)
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public void validateTripAccess(Long authenticatedUserId, Long tripId) {
        if (tripId == null) {
            throw new IllegalArgumentException("tripId is required");
        }
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("senderUserId is required");
        }
        validateAccessibleTrip(tripId, authenticatedUserId);
    }

    private void validateRequest(Long authenticatedUserId, ChatMessageRequest request) {
        if (request.tripId() == null) {
            throw new IllegalArgumentException("tripId is required");
        }
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("senderUserId is required");
        }
        if (request.content() == null || request.content().trim().isEmpty()) {
            throw new IllegalArgumentException("content must not be blank");
        }
        if (!chatMapper.existsUser(authenticatedUserId)) {
            throw new IllegalArgumentException("user not found: " + authenticatedUserId);
        }
        validateTripAccess(authenticatedUserId, request.tripId());
    }

    private void validateAccessibleTrip(Long tripId, Long userId) {
        if (!chatMapper.existsAccessibleTrip(tripId, userId)) {
            throw new IllegalArgumentException("trip not accessible: " + tripId);
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
