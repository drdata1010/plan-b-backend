package com.planb.supportticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {
        return messages
                // Allow all connections to the WebSocket endpoint
                .simpDestMatchers("/ws/**").permitAll()
                // Allow anyone to subscribe to public topics
                .simpSubscribeDestMatchers("/topic/public/**").permitAll()
                // Require authentication for user-specific destinations
                .simpSubscribeDestMatchers("/user/**").authenticated()
                // Require authentication for sending messages to application destinations
                .simpDestMatchers("/app/**").authenticated()
                // Require admin role for certain administrative topics
                .simpSubscribeDestMatchers("/topic/admin/**").hasRole("ADMIN")
                .anyMessage().denyAll()
                .build();
    }
}
