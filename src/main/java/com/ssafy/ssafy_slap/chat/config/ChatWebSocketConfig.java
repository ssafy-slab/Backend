package com.ssafy.ssafy_slap.chat.config;

import com.ssafy.ssafy_slap.chat.controller.ChatWebSocketHandler;
import com.ssafy.ssafy_slap.global.config.AppFrontendProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class ChatWebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final AppFrontendProperties frontendProperties;

    public ChatWebSocketConfig(ChatWebSocketHandler chatWebSocketHandler, AppFrontendProperties frontendProperties) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.frontendProperties = frontendProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chats")
                .setAllowedOriginPatterns(frontendProperties.allowedOriginPatterns().toArray(String[]::new));
    }
}
