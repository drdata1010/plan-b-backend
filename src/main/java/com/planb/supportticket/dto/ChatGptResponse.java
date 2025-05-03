package com.planb.supportticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for ChatGPT responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatGptResponse {
    
    private String response;
}
