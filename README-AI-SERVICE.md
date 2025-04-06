# AI Service Implementation Guide

This document explains the AI service implementation in the Support Ticket System.

## Overview

The AI service provides integration with multiple AI models for chat functionality. It supports:

- Multiple AI models (OpenAI, Anthropic, Google, Deepseek)
- WebSocket message handling
- Asynchronous response processing
- Error handling
- Session management

## Components

### AIService Interface

The `AIService` interface defines the contract for AI model interactions:

```java
public interface AIService {
    CompletableFuture<ChatMessage> processMessageAsync(ChatMessage message, String sessionId);
    List<AIModelType> getAvailableModels();
    Map<String, Object> getModelInfo(AIModelType modelType);
    boolean isModelAvailable(AIModelType modelType);
    String createSession(String userId, AIModelType modelType);
    void endSession(String sessionId);
    void clearConversationHistory(String sessionId);
    AIModelType getDefaultModel();
}
```

### AIServiceImpl Class

The `AIServiceImpl` class implements the `AIService` interface and provides:

- Integration with multiple AI model providers
- Conversation history management
- Asynchronous API calls
- Error handling and recovery
- Session management

### AIChatWebSocketController

The `AIChatWebSocketController` handles WebSocket messages for AI chat functionality:

- Processes chat messages from clients
- Routes messages to the appropriate AI model
- Manages AI chat rooms
- Handles session creation and cleanup

## Configuration

AI service configuration is defined in `application.yml`:

```yaml
# WebSocket AI Configuration
websocket:
  ai:
    enabled: ${WEBSOCKET_AI_ENABLED:true}
    default-model: ${WEBSOCKET_AI_DEFAULT_MODEL:gpt-3.5-turbo}
    timeout: ${WEBSOCKET_AI_TIMEOUT:30000}
    max-tokens: ${WEBSOCKET_AI_MAX_TOKENS:1000}
    temperature: ${WEBSOCKET_AI_TEMPERATURE:0.7}
    models:
      - id: gpt-3.5-turbo
        name: GPT-3.5 Turbo
        provider: openai
        enabled: ${WEBSOCKET_AI_MODEL_GPT35_ENABLED:true}
      - id: gpt-4
        name: GPT-4
        provider: openai
        enabled: ${WEBSOCKET_AI_MODEL_GPT4_ENABLED:true}
      - id: claude-2
        name: Claude 2
        provider: anthropic
        enabled: ${WEBSOCKET_AI_MODEL_CLAUDE2_ENABLED:false}
      - id: gemini-pro
        name: Gemini Pro
        provider: google
        enabled: ${WEBSOCKET_AI_MODEL_GEMINI_ENABLED:false}
      - id: custom
        name: Deepseek Chat
        provider: deepseek
        enabled: ${WEBSOCKET_AI_MODEL_DEEPSEEK_ENABLED:false}

# AI Provider Configuration
ai:
  openai:
    api-key: ${AI_OPENAI_API_KEY:}
    endpoint: ${AI_OPENAI_ENDPOINT:https://api.openai.com/v1/chat/completions}
  anthropic:
    api-key: ${AI_ANTHROPIC_API_KEY:}
    endpoint: ${AI_ANTHROPIC_ENDPOINT:https://api.anthropic.com/v1/messages}
  google:
    api-key: ${AI_GOOGLE_API_KEY:}
    endpoint: ${AI_GOOGLE_ENDPOINT:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}
  deepseek:
    api-key: ${AI_DEEPSEEK_API_KEY:}
    endpoint: ${AI_DEEPSEEK_ENDPOINT:https://api.deepseek.com/v1/chat/completions}
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `WEBSOCKET_AI_ENABLED` | Enable AI chat functionality | true |
| `WEBSOCKET_AI_DEFAULT_MODEL` | Default AI model to use | gpt-3.5-turbo |
| `WEBSOCKET_AI_TIMEOUT` | AI request timeout in milliseconds | 30000 |
| `WEBSOCKET_AI_MAX_TOKENS` | Maximum tokens for AI responses | 1000 |
| `WEBSOCKET_AI_TEMPERATURE` | AI response temperature (randomness) | 0.7 |
| `WEBSOCKET_AI_MODEL_GPT35_ENABLED` | Enable GPT-3.5 model | true |
| `WEBSOCKET_AI_MODEL_GPT4_ENABLED` | Enable GPT-4 model | true |
| `WEBSOCKET_AI_MODEL_CLAUDE2_ENABLED` | Enable Claude 2 model | false |
| `WEBSOCKET_AI_MODEL_GEMINI_ENABLED` | Enable Gemini Pro model | false |
| `WEBSOCKET_AI_MODEL_DEEPSEEK_ENABLED` | Enable Deepseek Chat model | false |
| `AI_OPENAI_API_KEY` | OpenAI API key | - |
| `AI_ANTHROPIC_API_KEY` | Anthropic API key | - |
| `AI_GOOGLE_API_KEY` | Google API key | - |
| `AI_DEEPSEEK_API_KEY` | Deepseek API key | - |

## WebSocket Endpoints

### Client-to-Server Endpoints

- `/app/ai.chat` - Send a message to an AI model
- `/app/ai.model/{modelId}` - Send a message to a specific AI model
- `/app/ai.models` - Get information about available AI models
- `/app/ai.clear/{roomId}` - Clear conversation history for a room
- `/app/ai.end/{roomId}` - End an AI chat session

### Server-to-Client Endpoints

- `/topic/room/{roomId}` - Receive AI responses in a chat room
- `/topic/room/{roomId}/typing` - Receive typing notifications
- `/user/queue/ai.room.created` - Receive notification when an AI chat room is created
- `/user/queue/errors` - Receive error messages

## Usage Example

### Client-Side Integration

```javascript
// Connect to WebSocket
const stompClient = new Client({
  webSocketFactory: () => new SockJS('/api/ws'),
  connectHeaders: {
    Authorization: 'Bearer ' + jwtToken
  }
});

stompClient.activate();

// Subscribe to AI responses
stompClient.onConnect = (frame) => {
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
  
  // Get available AI models
  stompClient.publish({
    destination: '/app/ai.models',
    body: JSON.stringify({})
  });
};

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

// Clear conversation history
function clearConversation(roomId) {
  stompClient.publish({
    destination: '/app/ai.clear/' + roomId,
    body: JSON.stringify({})
  });
}

// End AI chat session
function endAISession(roomId) {
  stompClient.publish({
    destination: '/app/ai.end/' + roomId,
    body: JSON.stringify({})
  });
}
```

## Implementation Details

### Session Management

The AI service maintains conversation history for each session:

- Each WebSocket session is associated with a chat room
- Conversation history is stored in memory
- Sessions can be cleared or ended by the user
- Inactive sessions are automatically cleaned up

### Error Handling

The AI service includes comprehensive error handling:

- API call timeouts
- Invalid responses from AI providers
- Authentication errors
- Rate limiting and quota errors
- Network errors

Errors are logged and sent to the client via WebSocket.

### Multiple Model Support

The AI service supports multiple AI models:

- **OpenAI Models**: GPT-3.5 Turbo, GPT-4
- **Anthropic Models**: Claude Instant, Claude 2
- **Google Models**: Gemini Pro
- **Deepseek Models**: Deepseek Chat

Each model has its own request and response format, which is handled by the service.

### Asynchronous Processing

All AI requests are processed asynchronously:

- API calls are made in a separate thread
- Responses are sent to the client via WebSocket
- Typing indicators are sent while waiting for responses
- Timeouts are handled gracefully

## Adding a New AI Model

To add a new AI model:

1. Add the model to the `AIModelType` enum
2. Add model configuration to `application.yml`
3. Implement request formatting and response parsing in `AIServiceImpl`
4. Add API key and endpoint configuration

Example for adding a new model:

```java
// 1. Add to AIModelType enum
public enum AIModelType {
    // Existing models...
    NEW_MODEL("new-model", "New Model", "Description of the new model");
}

// 2. In AIServiceImpl.init()
if (!newModelApiKey.isEmpty()) {
    modelConfigs.put(AIModelType.NEW_MODEL, new ModelConfig(
            "provider", 
            newModelEndpoint, 
            newModelApiKey, 
            true, 
            4096, 
            this::formatNewModelRequest, 
            this::parseNewModelResponse));
}

// 3. Implement request formatting
private HttpEntity<String> formatNewModelRequest(AISession session, ModelConfig config) {
    // Implementation...
}

// 4. Implement response parsing
private String parseNewModelResponse(String responseBody) {
    // Implementation...
}
```

## Security Considerations

- API keys are stored securely and not exposed to clients
- All WebSocket endpoints are secured with authentication
- Rate limiting is implemented to prevent abuse
- Conversation history is isolated between sessions
- Error messages do not expose sensitive information
