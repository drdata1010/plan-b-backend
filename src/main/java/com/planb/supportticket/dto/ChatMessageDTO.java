package com.planb.supportticket.dto;

import com.planb.supportticket.dto.websocket.AIModelType;
import com.planb.supportticket.entity.ChatMessage.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for chat messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private boolean read;
    private MessageType messageType;
    private AIModelType aiModel;
    private UUID senderId;
    private String senderName;
    private UUID chatSessionId;
}
