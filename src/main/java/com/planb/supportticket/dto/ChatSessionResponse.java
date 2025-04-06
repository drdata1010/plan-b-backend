package com.planb.supportticket.dto;

import com.planb.supportticket.entity.ChatSession.ChatSessionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for chat sessions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionResponse {
    
    private UUID id;
    private String title;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private boolean active;
    private ChatSessionType sessionType;
    private UUID userId;
    private String userName;
    private UUID expertId;
    private String expertName;
    private UUID ticketId;
    private String ticketTitle;
}
