package com.planb.supportticket.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for WebSocket events.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEvent {

    /**
     * Enum representing different types of WebSocket events.
     */
    public enum EventType {
        CONNECT,        // Client connected
        DISCONNECT,     // Client disconnected
        SUBSCRIBE,      // Client subscribed to a topic
        UNSUBSCRIBE,    // Client unsubscribed from a topic
        ERROR,          // Error occurred
        HEARTBEAT,      // Heartbeat message
        SESSION_EXPIRED // Session expired
    }

    /**
     * Type of event.
     */
    private EventType type;

    /**
     * User associated with the event.
     */
    private String username;

    /**
     * Session ID associated with the event.
     */
    private String sessionId;

    /**
     * Timestamp when the event occurred.
     */
    private LocalDateTime timestamp;

    /**
     * Topic associated with the event (for SUBSCRIBE and UNSUBSCRIBE events).
     */
    private String topic;

    /**
     * Error message (for ERROR events).
     */
    private String errorMessage;

    /**
     * Additional metadata for the event.
     */
    private Map<String, Object> metadata;

    /**
     * Factory method to create a connect event.
     */
    public static WebSocketEvent createConnectEvent(String username, String sessionId) {
        return WebSocketEvent.builder()
                .type(EventType.CONNECT)
                .username(username)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create a disconnect event.
     */
    public static WebSocketEvent createDisconnectEvent(String username, String sessionId) {
        return WebSocketEvent.builder()
                .type(EventType.DISCONNECT)
                .username(username)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create a subscribe event.
     */
    public static WebSocketEvent createSubscribeEvent(String username, String sessionId, String topic) {
        return WebSocketEvent.builder()
                .type(EventType.SUBSCRIBE)
                .username(username)
                .sessionId(sessionId)
                .topic(topic)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create an unsubscribe event.
     */
    public static WebSocketEvent createUnsubscribeEvent(String username, String sessionId, String topic) {
        return WebSocketEvent.builder()
                .type(EventType.UNSUBSCRIBE)
                .username(username)
                .sessionId(sessionId)
                .topic(topic)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create an error event.
     */
    public static WebSocketEvent createErrorEvent(String username, String sessionId, String errorMessage) {
        return WebSocketEvent.builder()
                .type(EventType.ERROR)
                .username(username)
                .sessionId(sessionId)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create a heartbeat event.
     */
    public static WebSocketEvent createHeartbeatEvent(String sessionId) {
        return WebSocketEvent.builder()
                .type(EventType.HEARTBEAT)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create a session expired event.
     */
    public static WebSocketEvent createSessionExpiredEvent(String username, String sessionId) {
        return WebSocketEvent.builder()
                .type(EventType.SESSION_EXPIRED)
                .username(username)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
