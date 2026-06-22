package com.ssafy.ssafy_slap.chat.mapper;

import com.ssafy.ssafy_slap.chat.domain.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMapper {

    boolean existsTrip(@Param("tripId") Long tripId);

    boolean existsUser(@Param("userId") Long userId);

    boolean existsAccessibleTrip(
            @Param("tripId") Long tripId,
            @Param("userId") Long userId
    );

    void insertMessage(@Param("message") ChatMessage message);

    ChatMessage findMessageById(@Param("messageId") Long messageId);

    List<ChatMessage> findRecentMessages(
            @Param("tripId") Long tripId,
            @Param("limit") int limit
    );
}
