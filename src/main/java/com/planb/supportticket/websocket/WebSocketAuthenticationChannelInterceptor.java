package com.planb.supportticket.websocket;

import com.planb.supportticket.security.firebase.FirebaseTokenValidator;
import com.planb.supportticket.security.firebase.FirebaseUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Channel interceptor for WebSocket authentication.
 * Validates Firebase tokens and sets the principal for WebSocket connections.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
    name = "firebase.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class WebSocketAuthenticationChannelInterceptor implements ChannelInterceptor {

    private final FirebaseTokenValidator firebaseTokenValidator;

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    /**
     * Intercepts messages before they are sent to the channel.
     * Validates Firebase tokens and sets the principal for WebSocket connections.
     *
     * @param message the message to intercept
     * @param channel the message channel
     * @return the intercepted message
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // Skip authentication if Firebase is disabled
            if (!firebaseEnabled) {
                log.info("Firebase authentication is disabled, allowing connection");
                // Create a mock user for development
                WebSocketAuthenticationPrincipal principal = new WebSocketAuthenticationPrincipal(
                        "mock-user-123",
                        UUID.fromString("00000000-0000-0000-0000-000000000000"),
                        new String[]{"ROLE_USER"}
                );
                accessor.setUser(principal);
                return message;
            }

            // Extract token from headers
            List<String> authorization = accessor.getNativeHeader("Authorization");

            if (authorization != null && !authorization.isEmpty()) {
                String bearerToken = authorization.get(0);

                if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                    String token = bearerToken.substring(7);

                    try {
                        // Validate token and get user details
                        FirebaseUserDetails userDetails = firebaseTokenValidator.validateToken(token);

                        // Extract roles
                        Set<String> roles = userDetails.getAuthorities().stream()
                                .map(authority -> authority.getAuthority())
                                .collect(java.util.stream.Collectors.toSet());

                        // Create custom principal
                        WebSocketAuthenticationPrincipal principal = new WebSocketAuthenticationPrincipal(
                                userDetails.getUid(),
                                UUID.fromString(userDetails.getUid()),
                                roles.toArray(new String[0])
                        );

                        // Set principal in accessor
                        accessor.setUser(principal);

                        log.debug("WebSocket authenticated user: {}", userDetails.getUsername());
                    } catch (BadCredentialsException e) {
                        log.debug("Invalid Firebase token in WebSocket connection: {}", e.getMessage());
                        throw e;
                    }
                }
            }
        }

        return message;
    }
}
