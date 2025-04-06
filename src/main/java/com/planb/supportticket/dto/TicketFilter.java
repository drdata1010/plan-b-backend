package com.planb.supportticket.dto;

import com.planb.supportticket.entity.enums.TicketPriority;
import com.planb.supportticket.entity.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Filter for ticket queries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketFilter {

    private UUID userId;
    private UUID expertId;
    private TicketStatus status;
    private TicketPriority priority;
    private String category;
    private String keyword;
}
