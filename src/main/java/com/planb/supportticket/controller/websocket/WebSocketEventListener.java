package com.planb.supportticket.controller.websocket;

import com.planb.supportticket.dto.websocket.ChatMessage;
import com.planb.supportticket.dto.websocket.WebSocketEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener for WebSocket events.
 * Handles connection, disconnection, subscription, and unsubscription events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    
    // Track active sessions
    private final Map<String, String> sessionIdToUsername = new ConcurrentHashMap<>();
    private final Map<String, String> sessionIdToRoomId = new ConcurrentHashMap<>();

    /**
     * Handles WebSocket connection events.
     * 
     * @param event the connection event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        String sessionId = headerAccessor.getSessionId();
        
        if (principal != null) {
            String username = principal.getName();
            sessionIdToUsername.put(sessionId, username);
            
            log.info("User connected: {}, session: {}", username, sessionId);
            
            // Create and publish connect event
            WebSocketEvent connectEvent = WebSocketEvent.createConnectEvent(username, sessionId);
            messagingTemplate.convertAndSend("/topic/events", connectEvent);
            
            // Also send to admin topic for monitoring
            messagingTemplate.convertAndSend("/topic/admin/events", connectEvent);
        }
    }

    /**
     * Handles WebSocket disconnection events.
     * 
     * @param event the disconnection event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        String username = sessionIdToUsername.remove(sessionId);
        String roomId = sessionIdToRoomId.remove(sessionId);
        
        if (username != null) {
            log.info("User disconnected: {}, session: {}", username, sessionId);
            
            // Create and publish disconnect event
            WebSocketEvent disconnectEvent = WebSocketEvent.createDisconnectEvent(username, sessionId);
            messagingTemplate.convertAndSend("/topic/events", disconnectEvent);
            
            // Also send to admin topic for monitoring
            messagingTemplate.convertAndSend("/topic/admin/events", disconnectEvent);
            
            // If user was in a chat room, send leave message
            if (roomId != null) {
                ChatMessage leaveMessage = ChatMessage.createLeaveMessage(roomId, username);
                messagingTemplate.convertAndSend("/topic/room/" + roomId, leaveMessage);
            }
        }
    }

    /**
     * Handles WebSocket subscription events.
     * 
     * @param event the subscription event
     */
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        if (principal != null && destination != null) {
            String username = principal.getName();
            
            log.debug("User {} subscribed to {}, session: {}", username, destination, sessionId);
            
            // Check if subscribing to a room topic
            if (destination.startsWith("/topic/room/")) {
                String roomId = destination.substring("/topic/room/".length());
                
                // If room ID contains additional segments (like /typing), extract just the room ID
                int slashIndex = roomId.indexOf('/');
                if (slashIndex > 0) {
                    roomId = roomId.substring(0, slashIndex);
                }
                
                // Store room association
                sessionIdToRoomId.put(sessionId, roomId);
                
                log.info("User {} joined room {}", username, roomId);
            }
            
            // Create and publish subscribe event
            WebSocketEvent subscribeEvent = WebSocketEvent.createSubscribeEvent(username, sessionId, destination);
            
            // Send to admin topic for monitoring
            messagingTemplate.convertAndSend("/topic/admin/events", subscribeEvent);
        }
    }

    /**
     * Handles WebSocket unsubscription events.
     * 
     * @param event the unsubscription event
     */
    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        if (principal != null && destination != null) {
            String username = principal.getName();
            
            log.debug("User {} unsubscribed from {}, session: {}", username, destination, sessionId);
            
            // Check if unsubscribing from a room topic
            if (destination != null && destination.startsWith("/topic/room/")) {
                String roomId = destination.substring("/topic/room/".length());
                
                // If room ID contains additional segments (like /typing), extract just the room ID
                int slashIndex = roomId.indexOf('/');
                if (slashIndex > 0) {
                    roomId = roomId.substring(0, slashIndex);
                }
                
                // Remove room association
                sessionIdToRoomId.remove(sessionId);
                
                log.info("User {} left room {}", username, roomId);
                
                // Send leave message to the room
                ChatMessage leaveMessage = ChatMessage.createLeaveMessage(roomId, username);
                messagingTemplate.convertAndSend("/topic/room/" + roomId, leaveMessage);
            }
            
            // Create and publish unsubscribe event
            WebSocketEvent unsubscribeEvent = WebSocketEvent.createUnsubscribeEvent(username, sessionId, destination);
            
            // Send to admin topic for monitoring
            messagingTemplate.convertAndSend("/topic/admin/events", unsubscribeEvent);
        }
    }
}
