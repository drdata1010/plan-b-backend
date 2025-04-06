package com.planb.supportticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for ticket comments.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCommentDTO {
    
    private UUID id;
    private String content;
    private boolean internalNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID userId;
    private String userName;
    private UUID parentId;
    private List<TicketCommentDTO> replies;
}
