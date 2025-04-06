package com.planb.supportticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Data Transfer Object for expert profiles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertDTO {
    
    private UUID id;
    private UUID userProfileId;
    private String bio;
    private Set<String> specializations;
    private BigDecimal hourlyRate;
    private Double rating;
    private boolean available;
    private LocalDateTime availableFrom;
    private LocalDateTime availableTo;
}
