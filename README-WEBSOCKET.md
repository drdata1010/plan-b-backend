# WebSocket Configuration Guide

This document explains how to use the WebSocket functionality in the Support Ticket System.

## Overview

The application uses WebSockets with STOMP for real-time communication between clients and the server. The implementation includes:

- Chat functionality between users
- AI-powered chat with multiple AI models
- Real-time notifications and events
- Secure WebSocket connections with authentication

## Configuration

WebSocket functionality is configured in `application.yml`:

```yaml
# WebSocket Configuration
websocket:
  allowed-origins: ${WEBSOCKET_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}
  endpoint: ${WEBSOCKET_ENDPOINT:/ws}
  topic-prefix: ${WEBSOCKET_TOPIC_PREFIX:/topic}
  queue-prefix: ${WEBSOCKET_QUEUE_PREFIX:/queue}
  app-prefix: ${WEBSOCKET_APP_PREFIX:/app}
  message:
    max-size: ${WEBSOCKET_MESSAGE_MAX_SIZE:65536}
    trace-enabled: ${WEBSOCKET_MESSAGE_TRACE_ENABLED:false}
  send-buffer-size: ${WEBSOCKET_SEND_BUFFER_SIZE:524288}
  send-time-limit: ${WEBSOCKET_SEND_TIME_LIMIT:15000}
  # AI Chat Configuration
  ai:
    enabled: ${WEBSOCKET_AI_ENABLED:true}
    default-model: ${WEBSOCKET_AI_DEFAULT_MODEL:gpt-3.5-turbo}
    timeout: ${WEBSOCKET_AI_TIMEOUT:30000}
```

## Environment Variables

The following environment variables can be set to configure WebSocket functionality:

### General WebSocket Configuration
- `WEBSOCKET_ALLOWED_ORIGINS` - Comma-separated list of allowed origins (default: http://localhost:3000,http://localhost:5173)
- `WEBSOCKET_ENDPOINT` - WebSocket endpoint path (default: /ws)
- `WEBSOCKET_TOPIC_PREFIX` - Prefix for broadcast topics (default: /topic)
- `WEBSOCKET_QUEUE_PREFIX` - Prefix for user-specific queues (default: /queue)
- `WEBSOCKET_APP_PREFIX` - Prefix for client-to-server messages (default: /app)

### Message Configuration
- `WEBSOCKET_MESSAGE_MAX_SIZE` - Maximum message size in bytes (default: 65536)
- `WEBSOCKET_MESSAGE_TRACE_ENABLED` - Whether to enable message tracing for debugging (default: false)
- `WEBSOCKET_SEND_BUFFER_SIZE` - Send buffer size in bytes (default: 524288)
- `WEBSOCKET_SEND_TIME_LIMIT` - Send timeout in milliseconds (default: 15000)

### AI Chat Configuration
- `WEBSOCKET_AI_ENABLED` - Whether AI chat is enabled (default: true)
- `WEBSOCKET_AI_DEFAULT_MODEL` - Default AI model to use (default: gpt-3.5-turbo)
- `WEBSOCKET_AI_TIMEOUT` - AI request timeout in milliseconds (default: 30000)

## WebSocket Endpoints

### Connection Endpoint
- `/ws` - Main WebSocket endpoint with SockJS support

### Chat Destinations

#### Client-to-Server Destinations (send to these)
- `/app/chat.room/{roomId}` - Send a message to a specific chat room
- `/app/chat.private` - Send a private message to another user
- `/app/chat.join/{roomId}` - Join a chat room
- `/app/chat.createRoom` - Create a new chat room
- `/app/chat.typing/{roomId}` - Send typing notification to a chat room

#### Server-to-Client Destinations (subscribe to these)
- `/topic/room/{roomId}` - Receive messages from a specific chat room
- `/topic/room/{roomId}/typing` - Receive typing notifications from a chat room
- `/user/queue/private` - Receive private messages
- `/user/queue/room.created` - Receive notification when a room is created
- `/user/queue/room.invitation` - Receive invitation to a chat room
- `/user/queue/errors` - Receive error messages

### AI Chat Destinations

#### Client-to-Server Destinations (send to these)
- `/app/ai.chat` - Send a message to an AI model
- `/app/ai.model/{modelId}` - Send a message to a specific AI model

#### Server-to-Client Destinations (subscribe to these)
- `/topic/room/{roomId}` - Receive AI responses in a chat room
- `/user/queue/ai.room.created` - Receive notification when an AI chat room is created

### Event Destinations

#### Server-to-Client Destinations (subscribe to these)
- `/topic/events` - Receive general WebSocket events
- `/topic/admin/events` - Receive administrative WebSocket events (requires ADMIN role)

## Data Transfer Objects

### ChatMessage
Represents a message in a chat conversation.

```java
public class ChatMessage {
    // Message types
    public enum MessageType {
        CHAT, JOIN, LEAVE, TYPING, AI_REQUEST, AI_RESPONSE, ERROR, SYSTEM
    }
    
    private String id;
    private String roomId;
    private MessageType type;
    private String content;
    private String sender;
    private String recipient;
    private LocalDateTime timestamp;
    private AIModelType aiModelType;
    private Map<String, Object> metadata;
    
    // Factory methods and other functionality...
}
```

### ChatRoom
Represents a chat room or conversation.

```java
public class ChatRoom {
    // Room types
    public enum RoomType {
        SUPPORT, AI, GROUP, PRIVATE, TICKET
    }
    
    private String id;
    private String name;
    private RoomType type;
    private String createdBy;
    private LocalDateTime createdAt;
    private Set<String> participants;
    private Long ticketId;
    private AIModelType aiModelType;
    private boolean active;
    
    // Factory methods and other functionality...
}
```

### AIModelType
Represents different AI models available for chat.

```java
public enum AIModelType {
    GPT_3_5("gpt-3.5-turbo", "GPT-3.5", "OpenAI's GPT-3.5 Turbo model"),
    GPT_4("gpt-4", "GPT-4", "OpenAI's GPT-4 model"),
    CLAUDE_INSTANT("claude-instant", "Claude Instant", "Anthropic's Claude Instant model"),
    CLAUDE_2("claude-2", "Claude 2", "Anthropic's Claude 2 model"),
    GEMINI_PRO("gemini-pro", "Gemini Pro", "Google's Gemini Pro model"),
    LLAMA_2("llama-2", "Llama 2", "Meta's Llama 2 model"),
    CUSTOM("custom", "Custom Model", "Custom in-house fine-tuned model");
    
    // Properties and methods...
}
```

### WebSocketEvent
Represents WebSocket connection events.

```java
public class WebSocketEvent {
    // Event types
    public enum EventType {
        CONNECT, DISCONNECT, SUBSCRIBE, UNSUBSCRIBE, ERROR, HEARTBEAT, SESSION_EXPIRED
    }
    
    private EventType type;
    private String username;
    private String sessionId;
    private LocalDateTime timestamp;
    private String topic;
    private String errorMessage;
    private Map<String, Object> metadata;
    
    // Factory methods and other functionality...
}
```

## Client-Side Integration

To connect to the WebSocket from a client (e.g., React):

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

// Create a new STOMP client
const stompClient = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/api/ws'),
  connectHeaders: {
    Authorization: 'Bearer ' + jwtToken
  },
  debug: function (str) {
    console.log(str);
  },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000
});

// Connect to the WebSocket
stompClient.activate();

// Subscribe to a chat room
stompClient.onConnect = (frame) => {
  const roomId = 'room123';
  
  // Subscribe to room messages
  stompClient.subscribe('/topic/room/' + roomId, (message) => {
    const chatMessage = JSON.parse(message.body);
    console.log('Received message:', chatMessage);
    // Update UI with the new message
  });
  
  // Subscribe to typing notifications
  stompClient.subscribe('/topic/room/' + roomId + '/typing', (message) => {
    const typingInfo = JSON.parse(message.body);
    console.log(typingInfo.sender + ' is typing...');
    // Show typing indicator in UI
  });
  
  // Subscribe to private messages
  stompClient.subscribe('/user/queue/private', (message) => {
    const privateMessage = JSON.parse(message.body);
    console.log('Private message:', privateMessage);
    // Update UI with the private message
  });
  
  // Subscribe to error messages
  stompClient.subscribe('/user/queue/errors', (message) => {
    const error = JSON.parse(message.body);
    console.error('Error:', error.content);
    // Show error in UI
  });
  
  // Join the room
  stompClient.publish({
    destination: '/app/chat.join/' + roomId,
    body: JSON.stringify({})
  });
};

// Send a message to a chat room
function sendMessage(roomId, content) {
  stompClient.publish({
    destination: '/app/chat.room/' + roomId,
    body: JSON.stringify({
      type: 'CHAT',
      content: content
    })
  });
}

// Send a private message
function sendPrivateMessage(recipient, content) {
  stompClient.publish({
    destination: '/app/chat.private',
    body: JSON.stringify({
      type: 'CHAT',
      recipient: recipient,
      content: content
    })
  });
}

// Send a message to an AI model
function sendAIMessage(roomId, content, modelType) {
  stompClient.publish({
    destination: '/app/ai.chat',
    body: JSON.stringify({
      roomId: roomId,
      type: 'AI_REQUEST',
      content: content,
      aiModelType: modelType
    })
  });
}

// Send typing notification
function sendTypingNotification(roomId) {
  stompClient.publish({
    destination: '/app/chat.typing/' + roomId,
    body: JSON.stringify({})
  });
}

// Create a new chat room
function createChatRoom(name, participants) {
  stompClient.publish({
    destination: '/app/chat.createRoom',
    body: JSON.stringify({
      name: name,
      type: 'GROUP',
      participants: participants
    })
  });
}

// Disconnect from WebSocket
function disconnect() {
  if (stompClient) {
    stompClient.deactivate();
  }
}
```

## Security

WebSocket connections are secured using Spring Security. The configuration includes:

- Authentication required for most message destinations
- Public access to connection endpoints
- Role-based access control for administrative topics
- JWT token validation for WebSocket connections

To authenticate WebSocket connections, include a JWT token in the connection headers:

```javascript
const stompClient = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/api/ws'),
  connectHeaders: {
    Authorization: 'Bearer ' + jwtToken
  },
  // Other configuration...
});
```

## Error Handling

Errors during WebSocket communication are sent to the client via the `/user/queue/errors` destination. Subscribe to this destination to receive error messages:

```javascript
stompClient.subscribe('/user/queue/errors', (message) => {
  const error = JSON.parse(message.body);
  console.error('Error:', error.content);
  // Show error in UI
});
```
