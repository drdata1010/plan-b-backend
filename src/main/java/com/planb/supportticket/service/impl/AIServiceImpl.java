package com.planb.supportticket.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planb.supportticket.dto.websocket.AIModelType;
import com.planb.supportticket.dto.websocket.ChatMessage;
import com.planb.supportticket.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of the AIService interface.
 * Provides support for multiple AI models, WebSocket message handling,
 * asynchronous response processing, error handling, and session management.
 */
@Service
@Slf4j
public class AIServiceImpl implements AIService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // AI model configuration
    @Value("${websocket.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${websocket.ai.default-model:gpt-3.5-turbo}")
    private String defaultModelId;

    @Value("${websocket.ai.timeout:30000}")
    private int aiTimeout;

    @Value("${websocket.ai.max-tokens:1000}")
    private int maxTokens;

    @Value("${websocket.ai.temperature:0.7}")
    private double temperature;

    // API keys for different providers
    @Value("${ai.openai.api-key:}")
    private String openaiApiKey;

    @Value("${ai.anthropic.api-key:}")
    private String anthropicApiKey;

    @Value("${ai.google.api-key:}")
    private String googleApiKey;

    @Value("${ai.deepseek.api-key:}")
    private String deepseekApiKey;

    // API endpoints for different providers
    @Value("${ai.openai.endpoint:https://api.openai.com/v1/chat/completions}")
    private String openaiEndpoint;

    @Value("${ai.anthropic.endpoint:https://api.anthropic.com/v1/messages}")
    private String anthropicEndpoint;

    @Value("${ai.google.endpoint:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String googleEndpoint;

    @Value("${ai.deepseek.endpoint:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekEndpoint;

    // Session management
    private final Map<String, AISession> sessions = new ConcurrentHashMap<>();

    // Available models
    private final Map<String, AIModelType> availableModels = new ConcurrentHashMap<>();

    // Model configuration
    private final Map<AIModelType, ModelConfig> modelConfigs = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Initialize available models
        for (AIModelType modelType : AIModelType.values()) {
            availableModels.put(modelType.getModelId(), modelType);
        }

        // Configure OpenAI models
        if (!openaiApiKey.isEmpty()) {
            modelConfigs.put(AIModelType.GPT_3_5, new ModelConfig(
                    "openai",
                    openaiEndpoint,
                    openaiApiKey,
                    true,
                    4096,
                    this::formatOpenAIRequest,
                    this::parseOpenAIResponse));

            modelConfigs.put(AIModelType.GPT_4, new ModelConfig(
                    "openai",
                    openaiEndpoint,
                    openaiApiKey,
                    true,
                    8192,
                    this::formatOpenAIRequest,
                    this::parseOpenAIResponse));
        } else {
            log.warn("OpenAI API key not configured. GPT models will not be available.");
        }

        // Configure Anthropic models
        if (!anthropicApiKey.isEmpty()) {
            modelConfigs.put(AIModelType.CLAUDE_INSTANT, new ModelConfig(
                    "anthropic",
                    anthropicEndpoint,
                    anthropicApiKey,
                    true,
                    4096,
                    this::formatAnthropicRequest,
                    this::parseAnthropicResponse));

            modelConfigs.put(AIModelType.CLAUDE_2, new ModelConfig(
                    "anthropic",
                    anthropicEndpoint,
                    anthropicApiKey,
                    true,
                    8192,
                    this::formatAnthropicRequest,
                    this::parseAnthropicResponse));
        } else {
            log.warn("Anthropic API key not configured. Claude models will not be available.");
        }

        // Configure Google models
        if (!googleApiKey.isEmpty()) {
            modelConfigs.put(AIModelType.GEMINI_PRO, new ModelConfig(
                    "google",
                    googleEndpoint + "?key=" + googleApiKey,
                    null,
                    true,
                    4096,
                    this::formatGoogleRequest,
                    this::parseGoogleResponse));
        } else {
            log.warn("Google API key not configured. Gemini models will not be available.");
        }

        // Configure Deepseek models (using CUSTOM model type)
        if (!deepseekApiKey.isEmpty()) {
            modelConfigs.put(AIModelType.CUSTOM, new ModelConfig(
                    "deepseek",
                    deepseekEndpoint,
                    deepseekApiKey,
                    true,
                    4096,
                    this::formatDeepseekRequest,
                    this::parseDeepseekResponse));
        }

        log.info("AI Service initialized with {} available models", modelConfigs.size());
    }

    @Override
    @Async
    public CompletableFuture<ChatMessage> processMessageAsync(ChatMessage message, String sessionId) {
        if (!aiEnabled) {
            return CompletableFuture.failedFuture(new IllegalStateException("AI service is disabled"));
        }

        AIModelType modelType = message.getAiModelType();
        if (modelType == null) {
            modelType = getDefaultModel();
            message.setAiModelType(modelType);
        }

        if (!isModelAvailable(modelType)) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("AI model not available: " + modelType.getDisplayName()));
        }

        // Get or create session
        final String finalSessionId = sessionId.toString();
        final String finalSender = message.getSender().toString();
        final AIModelType finalModelType = modelType;
        AISession session = sessions.computeIfAbsent(finalSessionId,
                id -> new AISession(id, finalSender, finalModelType));

        // Add user message to conversation history
        session.addMessage("user", message.getContent());

        // Get model configuration
        ModelConfig config = modelConfigs.get(modelType);

        // Create response CompletableFuture
        CompletableFuture<ChatMessage> responseFuture = new CompletableFuture<>();

        try {
            // Send "typing" indicator
            sendTypingIndicator(message.getRoomId(), modelType);

            // Format request based on model provider
            HttpEntity<String> requestEntity = config.requestFormatter.apply(session, config);

            // Make API call asynchronously
            CompletableFuture.supplyAsync(() -> {
                try {
                    ResponseEntity<String> response = restTemplate.postForEntity(
                            config.endpoint, requestEntity, String.class);
                    return response.getBody();
                } catch (Exception e) {
                    throw new RuntimeException("Error calling AI API: " + e.getMessage(), e);
                }
            })
            .orTimeout(aiTimeout, TimeUnit.MILLISECONDS)
            .thenApply(responseBody -> {
                try {
                    // Parse response based on model provider
                    String aiResponse = config.responseParser.apply(responseBody);

                    // Add AI response to conversation history
                    session.addMessage("assistant", aiResponse);

                    // Create response message
                    final String finalRoomId = message.getRoomId().toString();
                    final AIModelType finalModelType2 = finalModelType;
                    final String finalMessageId = message.getId().toString();
                    ChatMessage responseMessage = ChatMessage.createAIResponseMessage(
                            finalRoomId,
                            aiResponse,
                            finalModelType2,
                            finalMessageId
                    );

                    // Send response via WebSocket
                    final String destination = "/topic/room/" + finalRoomId;
                    messagingTemplate.convertAndSend(
                            destination,
                            responseMessage
                    );

                    return responseMessage;
                } catch (Exception e) {
                    throw new RuntimeException("Error parsing AI response: " + e.getMessage(), e);
                }
            })
            .whenComplete((result, error) -> {
                if (error != null) {
                    log.error("Error processing AI request", error);

                    // Send error message via WebSocket
                    String errorMessage = "Error processing AI request: " +
                            (error.getCause() != null ? error.getCause().getMessage() : error.getMessage());

                    ChatMessage errorResponse = ChatMessage.createErrorMessage(
                            message.getRoomId(),
                            errorMessage
                    );

                    messagingTemplate.convertAndSend(
                            "/topic/room/" + message.getRoomId(),
                            errorResponse
                    );

                    responseFuture.completeExceptionally(error);
                } else {
                    responseFuture.complete(result);
                }
            });

        } catch (Exception e) {
            log.error("Error initiating AI request", e);
            responseFuture.completeExceptionally(e);
        }

        return responseFuture;
    }

    @Override
    public List<AIModelType> getAvailableModels() {
        return modelConfigs.entrySet().stream()
                .filter(entry -> entry.getValue().enabled)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getModelInfo(AIModelType modelType) {
        ModelConfig config = modelConfigs.get(modelType);
        if (config == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> info = new HashMap<>();
        info.put("id", modelType.getModelId());
        info.put("name", modelType.getDisplayName());
        info.put("description", modelType.getDescription());
        info.put("provider", config.provider);
        info.put("maxTokens", config.maxTokens);
        info.put("enabled", config.enabled);

        return info;
    }

    @Override
    public boolean isModelAvailable(AIModelType modelType) {
        ModelConfig config = modelConfigs.get(modelType);
        return config != null && config.enabled;
    }

    @Override
    public String createSession(String userId, AIModelType modelType) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new AISession(sessionId, userId, modelType));
        return sessionId;
    }

    @Override
    public void endSession(String sessionId) {
        sessions.remove(sessionId);
    }

    @Override
    public void clearConversationHistory(String sessionId) {
        AISession session = sessions.get(sessionId);
        if (session != null) {
            session.clearHistory();
        }
    }

    @Override
    public AIModelType getDefaultModel() {
        AIModelType defaultModel = availableModels.get(defaultModelId);
        if (defaultModel != null && isModelAvailable(defaultModel)) {
            return defaultModel;
        }

        // If the configured default model is not available, return the first available model
        return getAvailableModels().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No AI models available"));
    }

    /**
     * Sends a typing indicator via WebSocket.
     *
     * @param roomId the room ID
     * @param modelType the AI model type
     */
    private void sendTypingIndicator(String roomId, AIModelType modelType) {
        ChatMessage typingMessage = new ChatMessage();
        typingMessage.setType(ChatMessage.MessageType.TYPING);
        typingMessage.setSender(modelType.getDisplayName());
        typingMessage.setRoomId(roomId);
        typingMessage.setTimestamp(LocalDateTime.now());
        typingMessage.setAiModelType(modelType);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/typing",
                typingMessage
        );
    }

    /**
     * Formats a request for the OpenAI API.
     *
     * @param session the AI session
     * @param config the model configuration
     * @return the HTTP entity for the request
     */
    private HttpEntity<String> formatOpenAIRequest(AISession session, ModelConfig config) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", session.getModelType().getModelId());
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            List<Map<String, String>> messages = new ArrayList<>();
            for (AISession.Message msg : session.getHistory()) {
                Map<String, String> message = new HashMap<>();
                message.put("role", msg.getRole());
                message.put("content", msg.getContent());
                messages.add(message);
            }
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.apiKey);

            return new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        } catch (Exception e) {
            throw new RuntimeException("Error formatting OpenAI request", e);
        }
    }

    /**
     * Parses a response from the OpenAI API.
     *
     * @param responseBody the response body
     * @return the AI response text
     */
    private String parseOpenAIResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode choicesNode = rootNode.path("choices");
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode firstChoice = choicesNode.get(0);
                JsonNode messageNode = firstChoice.path("message");
                return messageNode.path("content").asText();
            }
            throw new RuntimeException("Invalid response format from OpenAI API");
        } catch (Exception e) {
            throw new RuntimeException("Error parsing OpenAI response", e);
        }
    }

    /**
     * Formats a request for the Anthropic API.
     *
     * @param session the AI session
     * @param config the model configuration
     * @return the HTTP entity for the request
     */
    private HttpEntity<String> formatAnthropicRequest(AISession session, ModelConfig config) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", session.getModelType().getModelId());
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("temperature", temperature);

            List<Map<String, String>> messages = new ArrayList<>();
            for (AISession.Message msg : session.getHistory()) {
                Map<String, String> message = new HashMap<>();
                message.put("role", "user".equals(msg.getRole()) ? "human" : "assistant");
                message.put("content", msg.getContent());
                messages.add(message);
            }
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", config.apiKey);
            headers.set("anthropic-version", "2023-06-01");

            return new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        } catch (Exception e) {
            throw new RuntimeException("Error formatting Anthropic request", e);
        }
    }

    /**
     * Parses a response from the Anthropic API.
     *
     * @param responseBody the response body
     * @return the AI response text
     */
    private String parseAnthropicResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode contentNode = rootNode.path("content");
            if (contentNode.isArray() && contentNode.size() > 0) {
                JsonNode firstContent = contentNode.get(0);
                return firstContent.path("text").asText();
            }
            throw new RuntimeException("Invalid response format from Anthropic API");
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Anthropic response", e);
        }
    }

    /**
     * Formats a request for the Google API.
     *
     * @param session the AI session
     * @param config the model configuration
     * @return the HTTP entity for the request
     */
    private HttpEntity<String> formatGoogleRequest(AISession session, ModelConfig config) {
        try {
            Map<String, Object> requestBody = new HashMap<>();

            List<Map<String, Object>> contents = new ArrayList<>();
            for (AISession.Message msg : session.getHistory()) {
                Map<String, Object> content = new HashMap<>();
                content.put("role", "user".equals(msg.getRole()) ? "user" : "model");
                content.put("parts", List.of(Map.of("text", msg.getContent())));
                contents.add(content);
            }
            requestBody.put("contents", contents);

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", temperature);
            generationConfig.put("maxOutputTokens", maxTokens);
            requestBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        } catch (Exception e) {
            throw new RuntimeException("Error formatting Google request", e);
        }
    }

    /**
     * Parses a response from the Google API.
     *
     * @param responseBody the response body
     * @return the AI response text
     */
    private String parseGoogleResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode candidatesNode = rootNode.path("candidates");
            if (candidatesNode.isArray() && candidatesNode.size() > 0) {
                JsonNode firstCandidate = candidatesNode.get(0);
                JsonNode contentNode = firstCandidate.path("content");
                JsonNode partsNode = contentNode.path("parts");
                if (partsNode.isArray() && partsNode.size() > 0) {
                    return partsNode.get(0).path("text").asText();
                }
            }
            throw new RuntimeException("Invalid response format from Google API");
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Google response", e);
        }
    }

    /**
     * Formats a request for the Deepseek API.
     *
     * @param session the AI session
     * @param config the model configuration
     * @return the HTTP entity for the request
     */
    private HttpEntity<String> formatDeepseekRequest(AISession session, ModelConfig config) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            List<Map<String, String>> messages = new ArrayList<>();
            for (AISession.Message msg : session.getHistory()) {
                Map<String, String> message = new HashMap<>();
                message.put("role", msg.getRole());
                message.put("content", msg.getContent());
                messages.add(message);
            }
            requestBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.apiKey);

            return new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        } catch (Exception e) {
            throw new RuntimeException("Error formatting Deepseek request", e);
        }
    }

    /**
     * Parses a response from the Deepseek API.
     *
     * @param responseBody the response body
     * @return the AI response text
     */
    private String parseDeepseekResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode choicesNode = rootNode.path("choices");
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode firstChoice = choicesNode.get(0);
                JsonNode messageNode = firstChoice.path("message");
                return messageNode.path("content").asText();
            }
            throw new RuntimeException("Invalid response format from Deepseek API");
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Deepseek response", e);
        }
    }

    /**
     * Inner class representing an AI session.
     */
    private static class AISession {
        private final String id;
        private final String userId;
        private final AIModelType modelType;
        private final List<Message> history = new ArrayList<>();
        private LocalDateTime lastActivity = LocalDateTime.now();

        public AISession(String id, String userId, AIModelType modelType) {
            this.id = id;
            this.userId = userId;
            this.modelType = modelType;
        }

        public String getId() {
            return id;
        }

        public String getUserId() {
            return userId;
        }

        public AIModelType getModelType() {
            return modelType;
        }

        public List<Message> getHistory() {
            return history;
        }

        public LocalDateTime getLastActivity() {
            return lastActivity;
        }

        public void addMessage(String role, String content) {
            history.add(new Message(role, content));
            lastActivity = LocalDateTime.now();
        }

        public void clearHistory() {
            history.clear();
            lastActivity = LocalDateTime.now();
        }

        /**
         * Inner class representing a message in the conversation history.
         */
        public static class Message {
            private final String role;
            private final String content;

            public Message(String role, String content) {
                this.role = role;
                this.content = content;
            }

            public String getRole() {
                return role;
            }

            public String getContent() {
                return content;
            }
        }
    }

    /**
     * Inner class representing an AI model configuration.
     */
    private static class ModelConfig {
        private final String provider;
        private final String endpoint;
        private final String apiKey;
        private final boolean enabled;
        private final int maxTokens;
        private final RequestFormatter requestFormatter;
        private final ResponseParser responseParser;

        public ModelConfig(String provider, String endpoint, String apiKey, boolean enabled,
                          int maxTokens, RequestFormatter requestFormatter, ResponseParser responseParser) {
            this.provider = provider;
            this.endpoint = endpoint;
            this.apiKey = apiKey;
            this.enabled = enabled;
            this.maxTokens = maxTokens;
            this.requestFormatter = requestFormatter;
            this.responseParser = responseParser;
        }
    }

    /**
     * Functional interface for formatting API requests.
     */
    @FunctionalInterface
    private interface RequestFormatter {
        HttpEntity<String> apply(AISession session, ModelConfig config);
    }

    /**
     * Functional interface for parsing API responses.
     */
    @FunctionalInterface
    private interface ResponseParser {
        String apply(String responseBody);
    }
}
