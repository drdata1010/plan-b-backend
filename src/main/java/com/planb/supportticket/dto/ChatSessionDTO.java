package com.planb.supportticket.dto;

import com.planb.supportticket.entity.ChatSession.ChatSessionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Data Transfer Object for chat sessions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDTO {
    
    private String title;
    private ChatSessionType sessionType;
    private UUID expertId;
    private UUID ticketId;
}
