package com.planb.supportticket.dto;

import com.planb.supportticket.entity.enums.TicketPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for tickets.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {

    private String title;
    private String description;
    private TicketPriority priority;
    private String classification;
    private String area;
    private String detailedDescription;
    private LocalDateTime dueDate;
    private UUID assignedExpertId;
}
