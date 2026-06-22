package com.ssafy.ssafy_slap.chat.config;

import com.ssafy.ssafy_slap.chat.controller.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    public ChatWebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chats")
                .setAllowedOriginPatterns(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        "http://*:5173"
                );
    }
}
