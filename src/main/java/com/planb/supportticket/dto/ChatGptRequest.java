package com.planb.supportticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for ChatGPT requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptRequest {
    
    @NotBlank(message = "Message is required")
    private String message;
}
