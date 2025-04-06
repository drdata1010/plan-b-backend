package com.planb.supportticket.service;

import com.planb.supportticket.dto.websocket.AIModelType;
import com.planb.supportticket.dto.websocket.ChatMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for AI model interactions.
 * Provides methods for generating AI responses using different models.
 */
public interface AIService {

    /**
     * Processes a chat message and generates an AI response asynchronously.
     *
     * @param message the chat message to process
     * @param sessionId the session ID for tracking conversation history
     * @return a CompletableFuture that will complete with the AI response message
     */
    CompletableFuture<ChatMessage> processMessageAsync(ChatMessage message, String sessionId);
    
    /**
     * Gets a list of available AI models.
     *
     * @return a list of available AI model types
     */
    List<AIModelType> getAvailableModels();
    
    /**
     * Gets information about a specific AI model.
     *
     * @param modelType the AI model type
     * @return a map of model properties
     */
    Map<String, Object> getModelInfo(AIModelType modelType);
    
    /**
     * Checks if a specific AI model is available.
     *
     * @param modelType the AI model type to check
     * @return true if the model is available, false otherwise
     */
    boolean isModelAvailable(AIModelType modelType);
    
    /**
     * Creates a new chat session.
     *
     * @param userId the user ID
     * @param modelType the AI model type
     * @return the session ID
     */
    String createSession(String userId, AIModelType modelType);
    
    /**
     * Ends a chat session and cleans up resources.
     *
     * @param sessionId the session ID to end
     */
    void endSession(String sessionId);
    
    /**
     * Clears the conversation history for a session.
     *
     * @param sessionId the session ID
     */
    void clearConversationHistory(String sessionId);
    
    /**
     * Gets the default AI model type.
     *
     * @return the default AI model type
     */
    AIModelType getDefaultModel();
}
