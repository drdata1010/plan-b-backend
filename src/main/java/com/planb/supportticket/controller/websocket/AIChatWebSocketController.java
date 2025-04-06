package com.planb.supportticket.controller.websocket;

import com.planb.supportticket.dto.websocket.AIModelType;
import com.planb.supportticket.dto.websocket.ChatMessage;
import com.planb.supportticket.dto.websocket.ChatRoom;
import com.planb.supportticket.service.AIService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket controller for AI chat functionality.
 * Handles messages for different AI models and manages AI chat sessions.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AIChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    // AI service for model interactions
    private final AIService aiService;

    // In-memory store of active AI chat rooms (in a real app, use a persistent store)
    private final Map<String, ChatRoom> aiChatRooms = new ConcurrentHashMap<>();

    // Map of session IDs to room IDs
    private final Map<String, String> sessionToRoomMap = new ConcurrentHashMap<>();

    /**
     * Handles requests to chat with an AI model.
     *
     * @param message the chat message
     * @param headerAccessor the message headers
     * @param principal the authenticated user
     */
    @MessageMapping("/ai.chat")
    public void processAIChat(@Payload ChatMessage message,
                             SimpMessageHeaderAccessor headerAccessor,
                             Principal principal) {

        String username = principal.getName();
        log.debug("Received AI chat request from user: {}", username);

        // Validate the message
        if (message.getAiModelType() == null) {
            // Use default model if not specified
            message.setAiModelType(aiService.getDefaultModel());
        }

        // Check if the model is available
        if (!aiService.isModelAvailable(message.getAiModelType())) {
            sendErrorMessage(username, "AI model not available: " + message.getAiModelType().getDisplayName());
            return;
        }

        // Get or create a chat room for this AI conversation
        String roomId = message.getRoomId();
        if (roomId == null || roomId.isEmpty()) {
            // Create a new AI chat room
            ChatRoom room = ChatRoom.createAIRoom(username, message.getAiModelType());
            roomId = room.getId();
            aiChatRooms.put(roomId, room);
            message.setRoomId(roomId);

            // Notify the user about the new room
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/ai.room.created",
                room
            );
        }

        // Set message ID if not present
        if (message.getId() == null) {
            message.setId(java.util.UUID.randomUUID().toString());
        }

        // Send the user message to the room
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomId,
            message
        );

        // Generate a session ID if not exists
        String sessionId = headerAccessor.getSessionId();
        if (!sessionToRoomMap.containsKey(sessionId)) {
            sessionToRoomMap.put(sessionId, roomId);
        }

        // Process the AI request asynchronously using the service
        aiService.processMessageAsync(message, sessionId)
            .exceptionally(ex -> {
                log.error("Error processing AI request", ex);
                sendErrorMessage(username, "Error processing AI request: " + ex.getMessage());
                return null;
            });
    }

    /**
     * Handles requests to a specific AI model.
     *
     * @param modelId the AI model ID
     * @param message the chat message
     * @param principal the authenticated user
     */
    @MessageMapping("/ai.model/{modelId}")
    public void processModelSpecificChat(@DestinationVariable String modelId,
                                        @Payload ChatMessage message,
                                        Principal principal) {

        String username = principal.getName();
        log.debug("Received model-specific chat request from user: {} for model: {}", username, modelId);

        // Find the AI model type
        AIModelType modelType = AIModelType.findByModelId(modelId);
        if (modelType == null) {
            sendErrorMessage(username, "Unknown AI model: " + modelId);
            return;
        }

        // Set the AI model type in the message
        message.setAiModelType(modelType);

        // Process the message
        processAIChat(message, null, principal);
    }

    /**
     * Gets information about available AI models.
     *
     * @param principal the authenticated user
     * @return a map of model information
     */
    @MessageMapping("/ai.models")
    public List<Map<String, Object>> getAvailableModels(Principal principal) {
        String username = principal.getName();
        log.debug("User {} requested available AI models", username);

        List<AIModelType> models = aiService.getAvailableModels();

        return models.stream()
                .map(aiService::getModelInfo)
                .collect(Collectors.toList());
    }

    /**
     * Clears the conversation history for a session.
     *
     * @param roomId the room ID
     * @param principal the authenticated user
     */
    @MessageMapping("/ai.clear/{roomId}")
    public void clearConversation(@DestinationVariable String roomId, Principal principal) {
        String username = principal.getName();
        log.debug("User {} requested to clear conversation history for room {}", username, roomId);

        // Find the session ID for this room
        String sessionId = sessionToRoomMap.entrySet().stream()
                .filter(entry -> roomId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (sessionId != null) {
            aiService.clearConversationHistory(sessionId);

            // Send confirmation message
            ChatMessage systemMessage = new ChatMessage();
            systemMessage.setType(ChatMessage.MessageType.SYSTEM);
            systemMessage.setContent("Conversation history has been cleared.");
            systemMessage.setSender("System");
            systemMessage.setRoomId(roomId);
            systemMessage.setTimestamp(LocalDateTime.now());

            messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                systemMessage
            );
        } else {
            sendErrorMessage(username, "Could not find session for room: " + roomId);
        }
    }

    /**
     * Ends an AI chat session.
     *
     * @param roomId the room ID
     * @param principal the authenticated user
     */
    @MessageMapping("/ai.end/{roomId}")
    public void endSession(@DestinationVariable String roomId, Principal principal) {
        String username = principal.getName();
        log.debug("User {} requested to end AI session for room {}", username, roomId);

        // Find the session ID for this room
        String sessionId = sessionToRoomMap.entrySet().stream()
                .filter(entry -> roomId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (sessionId != null) {
            // End the session
            aiService.endSession(sessionId);
            sessionToRoomMap.remove(sessionId);
            aiChatRooms.remove(roomId);

            // Send confirmation message
            ChatMessage systemMessage = new ChatMessage();
            systemMessage.setType(ChatMessage.MessageType.SYSTEM);
            systemMessage.setContent("AI chat session has ended.");
            systemMessage.setSender("System");
            systemMessage.setRoomId(roomId);
            systemMessage.setTimestamp(LocalDateTime.now());

            messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                systemMessage
            );
        } else {
            sendErrorMessage(username, "Could not find session for room: " + roomId);
        }
    }

    /**
     * Sends an error message to a user.
     *
     * @param username the username
     * @param errorMessage the error message
     */
    private void sendErrorMessage(String username, String errorMessage) {
        ChatMessage error = ChatMessage.createErrorMessage(null, errorMessage);

        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/errors",
            error
        );

        log.error("Sent error to user {}: {}", username, errorMessage);
    }
}
