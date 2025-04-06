package com.planb.supportticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for consultations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationDTO {
    
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String notes;
    private UUID ticketId;
    private String meetingLink;
}
