package com.planb.supportticket.controller.websocket;

import com.planb.supportticket.dto.ChatMessageDTO;
import com.planb.supportticket.dto.ChatSessionDTO;
import com.planb.supportticket.dto.ChatSessionResponse;
import com.planb.supportticket.dto.websocket.AIModelType;
import com.planb.supportticket.dto.websocket.ChatMessage;
import com.planb.supportticket.entity.ChatSession;
import com.planb.supportticket.service.ChatService;
import com.planb.supportticket.websocket.ChatMessageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * Controller for WebSocket chat operations.
 * Handles chat messages, typing indicators, and AI interactions.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final ChatMessageProcessor chatMessageHandler;

    /**
     * Handles chat messages sent to a room.
     *
     * @param message the chat message
     * @param principal the authenticated user
     * @param headerAccessor the message header accessor
     * @return the processed message
     */
    @MessageMapping("/chat.sendSessionMessage")
    public ChatMessageDTO sendMessage(
            @Payload ChatMessageDTO message,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {

        log.debug("Received message from {}: {}", principal.getName(), message.getContent());
        return chatMessageHandler.handleChatMessage(message, principal, headerAccessor);
    }

    /**
     * Handles typing indicators.
     *
     * @param sessionId the chat session ID
     * @param principal the authenticated user
     */
    @MessageMapping("/chat.typing/{sessionId}")
    public void sendTypingIndicator(
            @DestinationVariable String sessionId,
            Principal principal) {

        try {
            UUID userId = UUID.fromString(principal.getName());
            String username = chatService.getUserProfileById(userId).getDisplayName();

            chatMessageHandler.sendTypingIndicator(UUID.fromString(sessionId), username);
        } catch (Exception e) {
            log.error("Error sending typing indicator", e);
        }
    }

    /**
     * Handles AI chat messages.
     *
     * Note: This method is disabled to avoid conflicts with AIChatWebSocketController.
     * Use AIChatWebSocketController.processAIChat instead.
     *
     * @param message the chat message
     * @param modelType the AI model type
     * @param principal the authenticated user
     * @param headerAccessor the message header accessor
     * @return the AI response message
     */
    // @MessageMapping("/ai.chat") - Disabled to avoid conflicts
    public ChatMessageDTO sendAIMessage(
            @Payload ChatMessageDTO message,
            @Payload(required = false) AIModelType modelType,
            Principal principal,
            SimpMessageHeaderAccessor headerAccessor) {

        log.debug("Received AI message from {}: {}", principal.getName(), message.getContent());
        return chatMessageHandler.handleAIChatMessage(message, modelType, principal, headerAccessor);
    }

    /**
     * Creates a new AI chat session.
     *
     * @param modelType the AI model type
     * @param principal the authenticated user
     * @return the created chat session
     */
    @MessageMapping("/ai.createSession")
    @SendToUser("/queue/ai.session.created")
    public ChatSessionResponse createAISession(
            @Payload(required = false) AIModelType modelType,
            Principal principal) {

        log.debug("Creating AI chat session for user: {}", principal.getName());

        ChatSession chatSession = chatMessageHandler.createAIChatSession(modelType, principal);

        return convertToSessionResponse(chatSession);
    }

    /**
     * Gets available AI models.
     *
     * Note: This method is disabled to avoid conflicts with AIChatWebSocketController.
     * Use AIChatWebSocketController.getAvailableModels instead.
     *
     * @return the list of available AI models
     */
    // @MessageMapping("/ai.models") - Disabled to avoid conflicts
    // @SendToUser("/queue/ai.models")
    public List<AIModelType> getAvailableAIModels() {
        return chatService.getAvailableAIModels();
    }

    /**
     * Creates a new chat session.
     *
     * @param chatSessionDTO the chat session data
     * @param principal the authenticated user
     * @return the created chat session
     */
    @MessageMapping("/chat.createSession")
    @SendToUser("/queue/chat.session.created")
    public ChatSessionResponse createChatSession(
            @Payload ChatSessionDTO chatSessionDTO,
            Principal principal) {

        log.debug("Creating chat session for user: {}", principal.getName());

        UUID userId = UUID.fromString(principal.getName());
        ChatSession chatSession = chatService.createChatSession(chatSessionDTO, userId);

        return convertToSessionResponse(chatSession);
    }

    /**
     * Joins a chat room.
     *
     * @param sessionId the chat session ID
     * @param principal the authenticated user
     */
    @MessageMapping("/chat.join/{sessionId}")
    public void joinChatRoom(
            @DestinationVariable String sessionId,
            Principal principal) {

        log.debug("User {} joining chat room: {}", principal.getName(), sessionId);

        try {
            UUID userId = UUID.fromString(principal.getName());
            String username = chatService.getUserProfileById(userId).getDisplayName();

            // Send system message to room
            ChatMessageDTO joinMessage = new ChatMessageDTO();
            joinMessage.setChatSessionId(UUID.fromString(sessionId));
            joinMessage.setContent(username + " joined the chat");
            joinMessage.setMessageType(com.planb.supportticket.entity.ChatMessage.MessageType.USER_JOINED);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + sessionId,
                    joinMessage
            );
        } catch (Exception e) {
            log.error("Error joining chat room", e);
        }
    }

    /**
     * Leaves a chat room.
     *
     * @param sessionId the chat session ID
     * @param principal the authenticated user
     */
    @MessageMapping("/chat.leave/{sessionId}")
    public void leaveChatRoom(
            @DestinationVariable String sessionId,
            Principal principal) {

        log.debug("User {} leaving chat room: {}", principal.getName(), sessionId);

        try {
            UUID userId = UUID.fromString(principal.getName());
            String username = chatService.getUserProfileById(userId).getDisplayName();

            // Send system message to room
            ChatMessageDTO leaveMessage = new ChatMessageDTO();
            leaveMessage.setChatSessionId(UUID.fromString(sessionId));
            leaveMessage.setContent(username + " left the chat");
            leaveMessage.setMessageType(com.planb.supportticket.entity.ChatMessage.MessageType.USER_LEFT);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + sessionId,
                    leaveMessage
            );
        } catch (Exception e) {
            log.error("Error leaving chat room", e);
        }
    }

    /**
     * Ends a chat session.
     *
     * @param sessionId the chat session ID
     * @param principal the authenticated user
     * @return the ended chat session
     */
    @MessageMapping("/chat.end/{sessionId}")
    @SendToUser("/queue/chat.session.ended")
    public ChatSessionResponse endChatSession(
            @DestinationVariable String sessionId,
            Principal principal) {

        log.debug("Ending chat session: {}", sessionId);

        ChatSession chatSession = chatService.endChatSession(UUID.fromString(sessionId));

        // Send system message to room
        ChatMessageDTO endMessage = new ChatMessageDTO();
        endMessage.setChatSessionId(UUID.fromString(sessionId));
        endMessage.setContent("Chat session ended");
        endMessage.setMessageType(com.planb.supportticket.entity.ChatMessage.MessageType.SYSTEM);

        messagingTemplate.convertAndSend(
                "/topic/room/" + sessionId,
                endMessage
        );

        return convertToSessionResponse(chatSession);
    }

    /**
     * Converts a ChatSession entity to a ChatSessionResponse DTO.
     *
     * @param chatSession the chat session entity
     * @return the chat session response DTO
     */
    private ChatSessionResponse convertToSessionResponse(ChatSession chatSession) {
        ChatSessionResponse response = new ChatSessionResponse();
        response.setId(chatSession.getId());
        response.setTitle(chatSession.getTitle());
        response.setStartedAt(chatSession.getStartedAt());
        response.setEndedAt(chatSession.getEndedAt());
        response.setActive(chatSession.isActive());
        response.setSessionType(chatSession.getSessionType());

        if (chatSession.getUser() != null) {
            response.setUserId(chatSession.getUser().getId());
            response.setUserName(chatSession.getUser().getDisplayName());
        }

        if (chatSession.getExpert() != null) {
            response.setExpertId(chatSession.getExpert().getId());
            if (chatSession.getExpert().getUserProfile() != null) {
                response.setExpertName(chatSession.getExpert().getUserProfile().getDisplayName());
            }
        }

        if (chatSession.getTicket() != null) {
            response.setTicketId(chatSession.getTicket().getId());
            response.setTicketTitle(chatSession.getTicket().getTitle());
        }

        return response;
    }
}
