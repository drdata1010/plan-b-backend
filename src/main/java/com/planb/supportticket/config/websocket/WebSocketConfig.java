package com.planb.supportticket.config.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * Configuration class for WebSocket support.
 * Enables and configures WebSocket message broker and endpoints.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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

    // STOMP Broker Relay Configuration
    @Value("${websocket.broker-relay.enabled:false}")
    private boolean brokerRelayEnabled;

    @Value("${websocket.broker-relay.host:localhost}")
    private String brokerRelayHost;

    @Value("${websocket.broker-relay.port:61613}")
    private int brokerRelayPort;

    @Value("${websocket.broker-relay.username:guest}")
    private String brokerRelayUsername;

    @Value("${websocket.broker-relay.password:guest}")
    private String brokerRelayPassword;

    @Value("${websocket.broker-relay.virtual-host:/}")
    private String brokerRelayVirtualHost;

    @Value("${websocket.broker-relay.heartbeat.client:10000}")
    private long brokerRelayClientHeartbeat;

    @Value("${websocket.broker-relay.heartbeat.server:10000}")
    private long brokerRelayServerHeartbeat;

    // Message Configuration
    @Value("${websocket.message.max-size:65536}")
    private int messageMaxSize;

    @Value("${websocket.send-buffer-size:524288}")
    private int sendBufferSize;

    @Value("${websocket.send-time-limit:15000}")
    private int sendTimeLimit;

    @Value("${websocket.message.buffer-size-per-session:1024}")
    private int bufferSizePerSession;

    /**
     * Configure the message broker for WebSocket communication.
     *
     * @param registry the MessageBrokerRegistry to configure
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Set prefix for messages from clients to server
        registry.setApplicationDestinationPrefixes(appPrefix);

        // Set prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");

        if (brokerRelayEnabled) {
            // Use external STOMP broker relay (for production)
            registry.enableStompBrokerRelay(topicPrefix, queuePrefix)
                    .setRelayHost(brokerRelayHost)
                    .setRelayPort(brokerRelayPort)
                    .setClientLogin(brokerRelayUsername)
                    .setClientPasscode(brokerRelayPassword)
                    .setSystemLogin(brokerRelayUsername)
                    .setSystemPasscode(brokerRelayPassword)
                    .setVirtualHost(brokerRelayVirtualHost)
                    // Commented out due to compatibility issues with the current Spring version
                    // .setClientHeartbeatSendInterval(brokerRelayClientHeartbeat)
                    // .setClientHeartbeatReceiveInterval(brokerRelayServerHeartbeat)
                    ;

            // Log that we're using external broker
            System.out.println("Using external STOMP broker: " + brokerRelayHost + ":" + brokerRelayPort);
        } else {
            // Enable a simple memory-based message broker to send messages to clients
            // on destinations prefixed with /topic (for broadcasts) and /queue (for user-specific messages)
            registry.enableSimpleBroker(topicPrefix, queuePrefix)
                    .setHeartbeatValue(new long[] {10000, 10000})
                    .setTaskScheduler(new org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler());

            // Log that we're using simple broker
            System.out.println("Using simple in-memory message broker");
        }
    }

    /**
     * Register STOMP endpoints for WebSocket communication.
     *
     * @param registry the StompEndpointRegistry to configure
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling SockJS fallback options
        registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS()
                .setHeartbeatTime(25000)
                .setDisconnectDelay(30000)
                .setClientLibraryUrl("https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js");
    }

    /**
     * Configure WebSocket transport options.
     *
     * @param registration the WebSocketTransportRegistration to configure
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
            .setMessageSizeLimit(messageMaxSize) // Max incoming message size
            .setSendBufferSizeLimit(sendBufferSize) // Max outgoing buffer size
            .setSendTimeLimit(sendTimeLimit); // Timeout for sending messages
            // Commented out due to compatibility issues
            // .setMessageCountLimitPerConnection(bufferSizePerSession); // Max messages per session
    }
}
