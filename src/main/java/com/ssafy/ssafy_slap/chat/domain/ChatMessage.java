package com.ssafy.ssafy_slap.chat.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class ChatMessage {

    private Long messageId;
    private Long tripId;
    private Long senderUserId;
    private String senderNickname;
    private String messageType;
    private String content;
    private LocalDateTime createdAt;

    public ChatMessage() {
    }

    public ChatMessage(
            Long messageId,
            Long tripId,
            Long senderUserId,
            String senderNickname,
            String messageType,
            String content,
            LocalDateTime createdAt
    ) {
        this.messageId = messageId;
        this.tripId = tripId;
        this.senderUserId = senderUserId;
        this.senderNickname = senderNickname;
        this.messageType = messageType;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatMessage that)) {
            return false;
        }
        return Objects.equals(messageId, that.messageId)
                && Objects.equals(tripId, that.tripId)
                && Objects.equals(senderUserId, that.senderUserId)
                && Objects.equals(senderNickname, that.senderNickname)
                && Objects.equals(messageType, that.messageType)
                && Objects.equals(content, that.content)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, tripId, senderUserId, senderNickname, messageType, content, createdAt);
    }
}
