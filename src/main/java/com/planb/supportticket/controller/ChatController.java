package com.planb.supportticket.controller;

import com.planb.supportticket.dto.ChatMessageDTO;
import com.planb.supportticket.dto.ChatSessionDTO;
import com.planb.supportticket.dto.ChatSessionResponse;
import com.planb.supportticket.dto.websocket.AIModelType;
import com.planb.supportticket.entity.Attachment;
import com.planb.supportticket.entity.ChatMessage;
import com.planb.supportticket.entity.ChatSession;
import com.planb.supportticket.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for chat operations.
 * Handles chat sessions, messages, and AI interactions.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final ChatService chatService;

    /**
     * Creates a new chat session.
     *
     * @param chatSessionDTO the chat session data
     * @param userDetails the authenticated user
     * @return the created chat session
     */
    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionResponse> createChatSession(
            @Valid @RequestBody ChatSessionDTO chatSessionDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        ChatSession chatSession = chatService.createChatSession(chatSessionDTO, userId);

        ChatSessionResponse response = convertToSessionResponse(chatSession);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets a chat session by ID.
     *
     * @param sessionId the chat session ID
     * @return the chat session
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionResponse> getChatSessionById(@PathVariable UUID sessionId) {
        ChatSession chatSession = chatService.getChatSessionById(sessionId);

        ChatSessionResponse response = convertToSessionResponse(chatSession);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets chat sessions for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    @GetMapping("/sessions/user/{userId}")
    public ResponseEntity<Page<ChatSessionResponse>> getChatSessionsByUserId(
            @PathVariable UUID userId,
            Pageable pageable) {

        Page<ChatSession> chatSessions = chatService.getChatSessionsByUserId(userId, pageable);

        Page<ChatSessionResponse> response = chatSessions.map(this::convertToSessionResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets chat sessions for an expert with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    @GetMapping("/sessions/expert/{expertId}")
    public ResponseEntity<Page<ChatSessionResponse>> getChatSessionsByExpertId(
            @PathVariable UUID expertId,
            Pageable pageable) {

        Page<ChatSession> chatSessions = chatService.getChatSessionsByExpertId(expertId, pageable);

        Page<ChatSessionResponse> response = chatSessions.map(this::convertToSessionResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets chat sessions for a ticket with pagination.
     *
     * @param ticketId the ticket ID
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    @GetMapping("/sessions/ticket/{ticketId}")
    public ResponseEntity<Page<ChatSessionResponse>> getChatSessionsByTicketId(
            @PathVariable UUID ticketId,
            Pageable pageable) {

        Page<ChatSession> chatSessions = chatService.getChatSessionsByTicketId(ticketId, pageable);

        Page<ChatSessionResponse> response = chatSessions.map(this::convertToSessionResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets active chat sessions for a user.
     *
     * @param userId the user ID
     * @return a list of active chat sessions
     */
    @GetMapping("/sessions/user/{userId}/active")
    public ResponseEntity<List<ChatSessionResponse>> getActiveChatSessionsForUser(@PathVariable UUID userId) {
        List<ChatSession> chatSessions = chatService.getActiveChatSessionsForUser(userId);

        List<ChatSessionResponse> response = chatSessions.stream()
                .map(this::convertToSessionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Gets active chat sessions for an expert.
     *
     * @param expertId the expert ID
     * @return a list of active chat sessions
     */
    @GetMapping("/sessions/expert/{expertId}/active")
    public ResponseEntity<List<ChatSessionResponse>> getActiveChatSessionsForExpert(@PathVariable UUID expertId) {
        List<ChatSession> chatSessions = chatService.getActiveChatSessionsForExpert(expertId);

        List<ChatSessionResponse> response = chatSessions.stream()
                .map(this::convertToSessionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Ends a chat session.
     *
     * @param sessionId the chat session ID
     * @return the ended chat session
     */
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<ChatSessionResponse> endChatSession(@PathVariable UUID sessionId) {
        ChatSession chatSession = chatService.endChatSession(sessionId);

        ChatSessionResponse response = convertToSessionResponse(chatSession);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds a message to a chat session.
     *
     * @param sessionId the chat session ID
     * @param messageDTO the message data
     * @param userDetails the authenticated user
     * @return the created chat message
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatMessageDTO> addMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody ChatMessageDTO messageDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        ChatMessage message = chatService.addMessage(sessionId, messageDTO, userId);

        ChatMessageDTO response = convertToMessageDTO(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets messages for a chat session with pagination.
     *
     * @param sessionId the chat session ID
     * @param pageable the pagination information
     * @return a page of chat messages
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Page<ChatMessageDTO>> getMessagesByChatSessionId(
            @PathVariable UUID sessionId,
            Pageable pageable) {

        Page<ChatMessage> messages = chatService.getMessagesByChatSessionId(sessionId, pageable);

        Page<ChatMessageDTO> response = messages.map(this::convertToMessageDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a chat message by ID.
     *
     * @param messageId the message ID
     * @return the chat message
     */
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<ChatMessageDTO> getMessageById(@PathVariable UUID messageId) {
        ChatMessage message = chatService.getMessageById(messageId);

        ChatMessageDTO response = convertToMessageDTO(message);
        return ResponseEntity.ok(response);
    }

    /**
     * Marks a message as read.
     *
     * @param messageId the message ID
     * @return the updated chat message
     */
    @PostMapping("/messages/{messageId}/read")
    public ResponseEntity<ChatMessageDTO> markMessageAsRead(@PathVariable UUID messageId) {
        ChatMessage message = chatService.markMessageAsRead(messageId);

        ChatMessageDTO response = convertToMessageDTO(message);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds an attachment to a chat message.
     *
     * @param messageId the message ID
     * @param file the file to attach
     * @param userDetails the authenticated user
     * @return the created attachment
     */
    @PostMapping(value = "/messages/{messageId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Attachment> addMessageAttachment(
            @PathVariable UUID messageId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        Attachment attachment = chatService.addMessageAttachment(messageId, file, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(attachment);
    }

    /**
     * Gets attachments for a chat message.
     *
     * @param messageId the message ID
     * @return a list of attachments
     */
    @GetMapping("/messages/{messageId}/attachments")
    public ResponseEntity<List<Attachment>> getMessageAttachments(@PathVariable UUID messageId) {
        List<Attachment> attachments = chatService.getMessageAttachments(messageId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * Sends a message to an AI model and gets a response.
     *
     * @param sessionId the chat session ID
     * @param messageDTO the message data
     * @param modelType the AI model type
     * @param userDetails the authenticated user
     * @return the AI response message
     */
    @PostMapping("/sessions/{sessionId}/ai")
    public ResponseEntity<ChatMessageDTO> sendMessageToAI(
            @PathVariable UUID sessionId,
            @Valid @RequestBody ChatMessageDTO messageDTO,
            @RequestParam(required = false) AIModelType modelType,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        ChatMessage message = chatService.sendMessageToAI(sessionId, messageDTO, userId, modelType);

        ChatMessageDTO response = convertToMessageDTO(message);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates an AI chat session.
     *
     * @param modelType the AI model type
     * @param userDetails the authenticated user
     * @return the created chat session
     */
    @PostMapping("/ai/sessions")
    public ResponseEntity<ChatSessionResponse> createAIChatSession(
            @RequestParam(required = false) AIModelType modelType,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        ChatSession chatSession = chatService.createAIChatSession(userId, modelType);

        ChatSessionResponse response = convertToSessionResponse(chatSession);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets available AI models.
     *
     * @return a list of available AI model types
     */
    @GetMapping("/ai/models")
    public ResponseEntity<List<AIModelType>> getAvailableAIModels() {
        List<AIModelType> models = chatService.getAvailableAIModels();
        return ResponseEntity.ok(models);
    }

    /**
     * Gets chat sessions by type with pagination.
     *
     * @param sessionType the session type
     * @param pageable the pagination information
     * @return a page of chat sessions
     */
    @GetMapping("/sessions/type")
    public ResponseEntity<Page<ChatSessionResponse>> getChatSessionsByType(
            @RequestParam ChatSession.ChatSessionType sessionType,
            Pageable pageable) {

        Page<ChatSession> chatSessions = chatService.getChatSessionsByType(sessionType, pageable);

        Page<ChatSessionResponse> response = chatSessions.map(this::convertToSessionResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets unread message count for a user.
     *
     * @param userId the user ID
     * @return the unread message count
     */
    @GetMapping("/messages/unread/count/{userId}")
    public ResponseEntity<Integer> getUnreadMessageCount(@PathVariable UUID userId) {
        int count = chatService.getUnreadMessageCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Gets unread messages for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of unread chat messages
     */
    @GetMapping("/messages/unread/{userId}")
    public ResponseEntity<Page<ChatMessageDTO>> getUnreadMessages(
            @PathVariable UUID userId,
            Pageable pageable) {

        Page<ChatMessage> messages = chatService.getUnreadMessages(userId, pageable);

        Page<ChatMessageDTO> response = messages.map(this::convertToMessageDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a chat message.
     *
     * @param messageId the message ID
     * @return no content
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
        chatService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a chat session and all its messages.
     *
     * @param sessionId the chat session ID
     * @return no content
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteChatSession(@PathVariable UUID sessionId) {
        chatService.deleteChatSession(sessionId);
        return ResponseEntity.noContent().build();
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

    /**
     * Converts a ChatMessage entity to a ChatMessageDTO.
     *
     * @param message the chat message entity
     * @return the chat message DTO
     */
    private ChatMessageDTO convertToMessageDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setRead(message.isRead());
        dto.setMessageType(message.getMessageType());
        if (message.getAiModel() != null) {
            try {
                dto.setAiModel(AIModelType.valueOf(message.getAiModel()));
            } catch (IllegalArgumentException e) {
                // If the AI model is not a valid enum value, set it to null
                dto.setAiModel(null);
            }
        }

        if (message.getSender() != null) {
            dto.setSenderId(message.getSender().getId());
            dto.setSenderName(message.getSender().getDisplayName());
        }

        if (message.getChatSession() != null) {
            dto.setChatSessionId(message.getChatSession().getId());
        }

        return dto;
    }

    /**
     * Gets the user ID from the UserDetails.
     *
     * @param userDetails the user details
     * @return the user ID
     */
    private UUID getUserIdFromUserDetails(UserDetails userDetails) {
        // In a real implementation, you would extract the user ID from the UserDetails
        // This is a placeholder implementation
        return UUID.fromString(userDetails.getUsername());
    }
}
