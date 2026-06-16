package com.ssafy.ssafy_slap.chat.service;

import com.ssafy.ssafy_slap.chat.domain.ChatMessage;
import com.ssafy.ssafy_slap.chat.dto.ChatMessageRequest;
import com.ssafy.ssafy_slap.chat.mapper.ChatMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    @Test
    void createsTextMessageWithTrimmedContent() {
        ChatMapper chatMapper = mock(ChatMapper.class);
        ChatService chatService = new ChatService(chatMapper);
        ChatMessageRequest request = new ChatMessageRequest(1L, 2L, "  hello  ");

        when(chatMapper.existsTrip(1L)).thenReturn(true);
        when(chatMapper.existsUser(2L)).thenReturn(true);
        doAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setMessageId(10L);
            return null;
        }).when(chatMapper).insertMessage(new ChatMessage(null, 1L, 2L, null, "TEXT", "hello", null));
        when(chatMapper.findMessageById(10L)).thenReturn(new ChatMessage(
                10L,
                1L,
                2L,
                "tester",
                "TEXT",
                "hello",
                LocalDateTime.of(2026, 6, 16, 10, 0)
        ));

        var response = chatService.createTextMessage(request);

        assertThat(response.messageId()).isEqualTo(10L);
        assertThat(response.content()).isEqualTo("hello");

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMapper).insertMessage(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getTripId()).isEqualTo(1L);
        assertThat(messageCaptor.getValue().getSenderUserId()).isEqualTo(2L);
        assertThat(messageCaptor.getValue().getMessageType()).isEqualTo("TEXT");
        assertThat(messageCaptor.getValue().getContent()).isEqualTo("hello");
    }

    @Test
    void rejectsBlankContent() {
        ChatMapper chatMapper = mock(ChatMapper.class);
        ChatService chatService = new ChatService(chatMapper);

        assertThatThrownBy(() -> chatService.createTextMessage(new ChatMessageRequest(1L, 2L, " ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("content must not be blank");
    }

    @Test
    void rejectsMissingTrip() {
        ChatMapper chatMapper = mock(ChatMapper.class);
        ChatService chatService = new ChatService(chatMapper);

        when(chatMapper.existsTrip(1L)).thenReturn(false);

        assertThatThrownBy(() -> chatService.createTextMessage(new ChatMessageRequest(1L, 2L, "hello")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("trip not found: 1");
    }
}
