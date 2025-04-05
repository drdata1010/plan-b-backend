package com.planb.supportticket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handles messages sent to /app/chat.sendMessage and broadcasts to all subscribers of /topic/public
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public Map<String, Object> sendMessage(@Payload Map<String, Object> message, Principal principal) {
        log.info("Received message: {}", message);
        
        // Add sender information
        message.put("sender", principal.getName());
        message.put("timestamp", System.currentTimeMillis());
        
        return message;
    }

    /**
     * Handles messages sent to /app/chat.addUser and broadcasts to all subscribers of /topic/public
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public Map<String, Object> addUser(@Payload Map<String, Object> message, 
                                      SimpMessageHeaderAccessor headerAccessor,
                                      Principal principal) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", principal.getName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "JOIN");
        response.put("sender", principal.getName());
        response.put("content", principal.getName() + " joined the chat");
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    /**
     * Sends a private message to a specific user
     */
    @MessageMapping("/chat.sendPrivateMessage")
    public void sendPrivateMessage(@Payload Map<String, Object> message, Principal principal) {
        String recipient = (String) message.get("recipient");
        
        // Add sender information
        message.put("sender", principal.getName());
        message.put("timestamp", System.currentTimeMillis());
        
        // Send to the specific user
        messagingTemplate.convertAndSendToUser(
                recipient,
                "/queue/messages",
                message
        );
        
        // Also send a copy to the sender
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/messages",
                message
        );
    }
}
