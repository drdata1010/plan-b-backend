package com.planb.supportticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Data Transfer Object for expert availability schedules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertAvailabilityDTO {
    
    private UUID id;
    private UUID expertId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
    private Integer maxConsultations;
    private String notes;
}
