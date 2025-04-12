package com.planb.supportticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple API application for testing.
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@RestController
@ComponentScan(basePackages = {"com.planb.supportticket.simple"})
public class SimpleApiApplication {

    public static void main(String[] args) {
        // Disable context path
        System.setProperty("server.servlet.context-path", "");
        // Use a different port to avoid conflicts
        System.setProperty("server.port", "8090");
        SpringApplication.run(SimpleApiApplication.class, args);
    }

    /**
     * Simple health check endpoint.
     * 
     * @return a simple response indicating the API is working
     */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Simple API is working");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
