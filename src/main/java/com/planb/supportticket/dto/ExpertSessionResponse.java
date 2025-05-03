package com.planb.supportticket.dto;

import com.planb.supportticket.entity.enums.ExpertSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for expert sessions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertSessionResponse {

    private UUID id;
    private LocalDateTime scheduledAt;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private ExpertSessionStatus status;
    private String meetingLink;
    private String notes;
    private Integer userRating;
    private String userFeedback;
    private String expertNotes;
    private String cancelledReason;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private UUID userId;
    private String userName;
    private UUID expertId;
    private String expertName;
    private UUID ticketId;
    private String ticketTitle;
}
