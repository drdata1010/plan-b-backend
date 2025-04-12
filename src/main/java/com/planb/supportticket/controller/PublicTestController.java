package com.planb.supportticket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Public test controller for testing API endpoints without authentication.
 */
@RestController
@RequestMapping("/public/test")
public class PublicTestController {

    /**
     * Simple test endpoint.
     * 
     * @return a simple response
     */
    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello from PublicTestController");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint for creating a ticket.
     * 
     * @param request the ticket request
     * @return a simple response
     */
    @PostMapping("/ticket")
    public ResponseEntity<Map<String, Object>> createTestTicket(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", UUID.randomUUID().toString());
        response.put("title", request.get("title"));
        response.put("description", request.get("description"));
        response.put("status", "OPEN");
        response.put("message", "Test ticket created successfully");
        return ResponseEntity.ok(response);
    }
}
