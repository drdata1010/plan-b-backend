package com.planb.supportticket.dto.websocket;

/**
 * Enum representing different AI model types available for chat.
 */
public enum AIModelType {
    GPT_3_5("gpt-3.5-turbo", "GPT-3.5", "OpenAI's GPT-3.5 Turbo model"),
    GPT_4("gpt-4", "GPT-4", "OpenAI's GPT-4 model"),
    CLAUDE_INSTANT("claude-instant", "Claude Instant", "Anthropic's Claude Instant model"),
    CLAUDE_2("claude-2", "Claude 2", "Anthropic's Claude 2 model"),
    GEMINI_PRO("gemini-pro", "Gemini Pro", "Google's Gemini Pro model"),
    LLAMA_2("llama-2", "Llama 2", "Meta's Llama 2 model"),
    CUSTOM("custom", "Custom Model", "Custom in-house fine-tuned model");

    private final String modelId;
    private final String displayName;
    private final String description;

    AIModelType(String modelId, String displayName, String description) {
        this.modelId = modelId;
        this.displayName = displayName;
        this.description = description;
    }

    public String getModelId() {
        return modelId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Find an AIModelType by its model ID.
     *
     * @param modelId the model ID to search for
     * @return the matching AIModelType or null if not found
     */
    public static AIModelType findByModelId(String modelId) {
        for (AIModelType type : values()) {
            if (type.getModelId().equals(modelId)) {
                return type;
            }
        }
        return null;
    }
}
