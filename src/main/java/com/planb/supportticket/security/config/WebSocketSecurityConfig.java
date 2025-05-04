package com.planb.supportticket.security.config;

import com.planb.supportticket.security.firebase.FirebaseTokenValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.List;

/**
 * Configuration for WebSocket security.
 * Configures WebSocket authentication and authorization.
 */
@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocketSecurity
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSecurityConfig.class);

    @Autowired
    private FirebaseTokenValidator firebaseTokenValidator;

    @Value("${websocket.allowed-origins:*}")
    private String[] allowedOrigins;

    @Value("${websocket.endpoint:/ws}")
    private String endpoint;

    @Value("${websocket.topic-prefix:/topic}")
    private String topicPrefix;

    @Value("${websocket.queue-prefix:/queue}")
    private String queuePrefix;

    @Value("${websocket.app-prefix:/app}")
    private String appPrefix;

    @Value("${websocket.security.require-authentication:false}")
    private boolean requireAuthentication;

    @Value("${websocket.security.admin-topics-role:ROLE_ADMIN}")
    private String adminTopicsRole;

    @Value("${websocket.security.support-topics-role:ROLE_SUPPORT}")
    private String supportTopicsRole;

    /**
     * Configures the message broker for WebSocket communication.
     *
     * @param registry the MessageBrokerRegistry to configure
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple memory-based message broker to send messages to clients
        registry.enableSimpleBroker(topicPrefix, queuePrefix);

        // Set prefix for messages from clients to server
        registry.setApplicationDestinationPrefixes(appPrefix);

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
        registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS();
    }

    /**
     * Configures the client inbound channel to authenticate WebSocket messages.
     *
     * @param registration the ChannelRegistration to configure
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
            // Add security context interceptor
            new SecurityContextChannelInterceptor(),

            // Add custom interceptor for Firebase token authentication
            new ChannelInterceptor() {
                @Override
                public Message<?> preSend(Message<?> message, MessageChannel channel) {
                    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                        // Extract token from headers
                        List<String> authorization = accessor.getNativeHeader("Authorization");

                        if (authorization != null && !authorization.isEmpty()) {
                            String bearerToken = authorization.get(0);

                            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                                String token = bearerToken.substring(7);

                                try {
                                    // Validate token and get user details
                                    var userDetails = firebaseTokenValidator.validateToken(token);

                                    // Create authenticated token
                                    Authentication auth = new com.planb.supportticket.security.firebase.FirebaseAuthenticationToken(
                                            userDetails, token, userDetails.getAuthorities());

                                    // Set authentication in accessor
                                    accessor.setUser(auth);

                                    logger.debug("WebSocket authenticated user: {}", userDetails.getUsername());
                                } catch (BadCredentialsException e) {
                                    logger.debug("Invalid Firebase token in WebSocket connection: {}", e.getMessage());
                                    // If authentication is required, throw exception to reject connection
                                    if (requireAuthentication) {
                                        throw e;
                                    }
                                }
                            }
                        } else if (requireAuthentication) {
                            // If authentication is required and no token provided, reject connection
                            throw new BadCredentialsException("No authentication token provided");
                        }
                    }

                    return message;
                }
            }
        );
    }

    /**
     * Configures authorization rules for WebSocket messages.
     *
     * @param messages the builder for creating message matchers
     * @return the configured AuthorizationManager for WebSocket messages
     */
    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {

        MessageMatcherDelegatingAuthorizationManager.Builder builder = messages
                // Allow all connections to the WebSocket endpoint
                .simpDestMatchers("/ws/**").permitAll()

                // Allow anyone to subscribe to public topics
                .simpSubscribeDestMatchers("/topic/public/**").permitAll();

        // Configure authentication requirements based on configuration
        if (requireAuthentication) {
            // Require authentication for most destinations
            builder
                // Allow authenticated users to subscribe to chat rooms
                .simpSubscribeDestMatchers("/topic/room/**").authenticated()

                // Allow authenticated users to subscribe to AI chat topics
                .simpSubscribeDestMatchers("/topic/ai/**").authenticated()

                // Require authentication for user-specific destinations
                .simpSubscribeDestMatchers("/user/**").authenticated()

                // Require authentication for sending messages to application destinations
                .simpDestMatchers("/app/**").authenticated();
        } else {
            // Allow public access to most destinations (for development/testing)
            builder
                .simpSubscribeDestMatchers("/topic/room/**").permitAll()
                .simpSubscribeDestMatchers("/topic/ai/**").permitAll()
                .simpSubscribeDestMatchers("/user/**").permitAll()
                .simpDestMatchers("/app/**").permitAll();
        }

        // Configure role-based access control
        builder
            // Require admin role for administrative topics
            .simpSubscribeDestMatchers("/topic/admin/**").hasRole(adminTopicsRole.replace("ROLE_", ""))
            .simpDestMatchers("/app/admin/**").hasRole(adminTopicsRole.replace("ROLE_", ""))

            // Require support role for support topics
            .simpSubscribeDestMatchers("/topic/support/**").hasRole(supportTopicsRole.replace("ROLE_", ""))
            .simpDestMatchers("/app/support/**").hasRole(supportTopicsRole.replace("ROLE_", ""))

            // Deny all other messages by default
            .anyMessage().denyAll();

        return builder.build();
    }
}
