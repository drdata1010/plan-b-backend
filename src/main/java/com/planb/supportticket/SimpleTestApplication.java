package com.planb.supportticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple test application with security disabled.
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@RestController
public class SimpleTestApplication {

    public static void main(String[] args) {
        System.setProperty("server.port", "8081");
        System.setProperty("server.servlet.context-path", "");
        SpringApplication.run(SimpleTestApplication.class, args);
    }

    /**
     * Simple test endpoint.
     * 
     * @return a simple response
     */
    @GetMapping("/simple-test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Simple test endpoint is working");
        return response;
    }
}
