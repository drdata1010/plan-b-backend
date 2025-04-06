package com.planb.supportticket.config;

import com.planb.supportticket.websocket.WebSocketAuthenticationChannelInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration for WebSocket messaging.
 * Enables WebSocket support and configures message broker.
 */
@Configuration
@EnableWebSocketMessageBroker
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired(required = false)
    private WebSocketAuthenticationChannelInterceptor authInterceptor;

    @Value("${websocket.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    /**
     * Configures the message broker for WebSocket communication.
     *
     * @param registry the MessageBrokerRegistry to configure
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple memory-based message broker to send messages to clients
        // - /topic is used for one-to-many (broadcast) messaging
        // - /queue is used for one-to-one (private) messaging
        registry.enableSimpleBroker("/topic", "/queue");

        // Set prefix for messages from clients to server
        registry.setApplicationDestinationPrefixes("/app");

        // Set prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints for WebSocket communication.
     *
     * @param registry the StompEndpointRegistry to configure
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the WebSocket endpoint, enabling SockJS fallback options
        registry.addEndpoint("/ws")
               .setAllowedOrigins(allowedOrigins)
               .withSockJS();
    }

    /**
     * Configures the client inbound channel to authenticate WebSocket messages.
     *
     * @param registration the ChannelRegistration to configure
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add authentication interceptor if Firebase is enabled
        if (firebaseEnabled) {
            registration.interceptors(authInterceptor);
        }
    }
}
