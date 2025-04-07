package com.planb.supportticket.config.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.HandshakeInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

/**
 * Advanced configuration for STOMP over WebSocket.
 * Configures interceptors, message size limits, and other STOMP-specific settings.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class StompConfig {

    @Value("${websocket.message.max-size:65536}")
    private int messageMaxSize;

    @Value("${websocket.send-buffer-size:524288}")
    private int sendBufferSize;

    @Value("${websocket.send-time-limit:15000}")
    private int sendTimeLimit;

    @Value("${websocket.message.trace-enabled:false}")
    private boolean traceEnabled;

    // Inject the interceptors
    private final StompHandshakeInterceptor stompHandshakeInterceptor;
    private final StompAuthenticationInterceptor stompAuthenticationInterceptor;

    /**
     * Configures WebSocket transport settings.
     *
     * @return the configured WebSocketTransportRegistration
     */
    @Bean
    public WebSocketTransportRegistration webSocketTransportRegistration() {
        return new WebSocketTransportRegistration()
                .setMessageSizeLimit(messageMaxSize) // Max incoming message size
                .setSendBufferSizeLimit(sendBufferSize) // Max outgoing buffer size
                .setSendTimeLimit(sendTimeLimit) // Timeout for sending messages
                .addDecoratorFactory(new UserTrackingHandshakeInterceptor()); // Track user sessions
    }

    /**
     * Configures the client inbound channel.
     * This channel handles messages from clients to the server.
     *
     * @return the configured ChannelRegistration
     */
    @Bean
    public ChannelRegistration configureClientInboundChannel() {
        ChannelRegistration registration = new ChannelRegistration();
        // Add authentication interceptor to validate user tokens
        registration.interceptors(stompAuthenticationInterceptor);

        // Configure thread pool for processing incoming messages
        registration.taskExecutor()
                .corePoolSize(4)
                .maxPoolSize(10)
                .queueCapacity(100);

        return registration;
    }

    /**
     * Configures the client outbound channel.
     * This channel handles messages from the server to clients.
     *
     * @return the configured ChannelRegistration
     */
    @Bean
    public ChannelRegistration configureClientOutboundChannel() {
        ChannelRegistration registration = new ChannelRegistration();
        // Configure thread pool for sending messages to clients
        registration.taskExecutor()
                .corePoolSize(4)
                .maxPoolSize(10)
                .queueCapacity(100);

        // Add tracing interceptor if enabled
        if (traceEnabled) {
            registration.interceptors(createTracingInterceptor());
        }

        return registration;
    }

    /**
     * Creates a tracing interceptor for debugging WebSocket messages.
     *
     * @return the ChannelInterceptor for tracing
     */
    private ChannelInterceptor createTracingInterceptor() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                log.debug("Outbound message: {}", message);
                return message;
            }
        };
    }

    /**
     * STOMP handshake interceptor for handling WebSocket handshakes.
     * Adds attributes to the WebSocket session during handshake.
     */
    @Component
    public static class StompHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                      WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            // Extract information from the HTTP request and add to WebSocket session attributes
            String remoteAddress = request.getRemoteAddress() != null ?
                    request.getRemoteAddress().getHostString() : "unknown";
            attributes.put("remoteAddress", remoteAddress);
            attributes.put("timestamp", System.currentTimeMillis());

            log.debug("WebSocket handshake from {}", remoteAddress);
            return true; // Allow the handshake to proceed
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                  WebSocketHandler wsHandler, Exception exception) {
            // Can be used for logging or cleanup after handshake
            if (exception != null) {
                log.error("Handshake failed: {}", exception.getMessage());
            }
        }
    }

    /**
     * STOMP authentication interceptor for validating WebSocket messages.
     * Checks authentication tokens and enforces security policies.
     */
    @Component
    public static class StompAuthenticationInterceptor implements ChannelInterceptor {

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                // Extract authentication information from headers
                String token = accessor.getFirstNativeHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    token = token.substring(7);

                    // In a real application, you would validate the token here
                    // and set the authenticated user principal

                    // For demonstration purposes, we'll create a simple principal
                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() {
                            return "user123"; // In a real app, extract from token
                        }
                    });

                    log.debug("User authenticated via token");
                }
            }

            return message;
        }
    }

    /**
     * User tracking handshake interceptor for monitoring WebSocket connections.
     * Decorates WebSocket handlers to track user sessions.
     */
    public static class UserTrackingHandshakeInterceptor implements WebSocketHandlerDecoratorFactory {

        @Override
        public WebSocketHandler decorate(final WebSocketHandler handler) {
            return new WebSocketHandlerDecorator(handler) {
                @Override
                public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                    // Track the new connection
                    String user = getUsernameFromSession(session);
                    log.debug("WebSocket connection established for user: {}", user);
                    super.afterConnectionEstablished(session);
                }

                @Override
                public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                    // Track the closed connection
                    String user = getUsernameFromSession(session);
                    log.debug("WebSocket connection closed for user: {}, status: {}", user, closeStatus);
                    super.afterConnectionClosed(session, closeStatus);
                }

                private String getUsernameFromSession(WebSocketSession session) {
                    Principal principal = session.getPrincipal();
                    return principal != null ? principal.getName() : "anonymous";
                }
            };
        }
    }
}
