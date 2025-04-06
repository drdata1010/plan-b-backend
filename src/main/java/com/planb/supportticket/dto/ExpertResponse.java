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
 * Response DTO for expert profiles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertResponse {
    
    private UUID id;
    private UUID userId;
    private String displayName;
    private String email;
    private String profilePictureUrl;
    private String bio;
    private Set<String> specializations;
    private BigDecimal hourlyRate;
    private Double rating;
    private boolean available;
    private LocalDateTime availableFrom;
    private LocalDateTime availableTo;
}
