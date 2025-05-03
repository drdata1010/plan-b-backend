package com.planb.supportticket.controller;

import com.planb.supportticket.dto.ExpertSessionDTO;
import com.planb.supportticket.dto.ExpertSessionResponse;
import com.planb.supportticket.entity.ExpertSession;
import com.planb.supportticket.service.ExpertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for expert session operations.
 * Handles expert session scheduling, management, and feedback.
 */
@RestController
@RequestMapping("/expert-sessions")
@RequiredArgsConstructor
@Slf4j
public class ExpertSessionController {
    private final ExpertService expertService;

    /**
     * Schedules a session with an expert.
     *
     * @param expertId the expert ID
     * @param sessionDTO the session data
     * @param userDetails the authenticated user
     * @return the scheduled session
     */
    @PostMapping("/expert/{expertId}")
    public ResponseEntity<ExpertSessionResponse> scheduleSession(
            @PathVariable UUID expertId,
            @Valid @RequestBody ExpertSessionDTO sessionDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        ExpertSession session = expertService.scheduleExpertSession(expertId, sessionDTO, userId);

        ExpertSessionResponse response = convertToResponse(session);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets a session by ID.
     *
     * @param id the session ID
     * @return the session
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExpertSessionResponse> getSession(@PathVariable UUID id) {
        ExpertSession session = expertService.getExpertSessionById(id);

        ExpertSessionResponse response = convertToResponse(session);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a session.
     *
     * @param id the session ID
     * @param sessionDTO the updated session data
     * @return the updated session
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExpertSessionResponse> updateSession(
            @PathVariable UUID id,
            @Valid @RequestBody ExpertSessionDTO sessionDTO) {

        ExpertSession session = expertService.updateExpertSession(id, sessionDTO);

        ExpertSessionResponse response = convertToResponse(session);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels a session.
     *
     * @param id the session ID
     * @param reason the cancellation reason
     * @param userDetails the authenticated user
     * @return the cancelled session
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ExpertSessionResponse> cancelSession(
            @PathVariable UUID id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        ExpertSession session = expertService.cancelExpertSession(id, reason, userId);

        ExpertSessionResponse response = convertToResponse(session);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets sessions for an expert with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of sessions
     */
    @GetMapping("/expert/{expertId}")
    public ResponseEntity<Page<ExpertSessionResponse>> getSessionsByExpertId(
            @PathVariable UUID expertId,
            Pageable pageable) {

        Page<ExpertSession> sessions = expertService.getExpertSessionsByExpertId(expertId, pageable);

        Page<ExpertSessionResponse> response = sessions.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets sessions for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of sessions
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ExpertSessionResponse>> getSessionsByUserId(
            @PathVariable UUID userId,
            Pageable pageable) {

        Page<ExpertSession> sessions = expertService.getExpertSessionsByUserId(userId, pageable);

        Page<ExpertSessionResponse> response = sessions.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets upcoming sessions for an expert.
     *
     * @param expertId the expert ID
     * @return a list of upcoming sessions
     */
    @GetMapping("/expert/{expertId}/upcoming")
    public ResponseEntity<List<ExpertSessionResponse>> getUpcomingSessionsForExpert(
            @PathVariable UUID expertId) {

        List<ExpertSession> sessions = expertService.getUpcomingSessionsForExpert(expertId);

        List<ExpertSessionResponse> response = sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Gets upcoming sessions for a user.
     *
     * @param userId the user ID
     * @return a list of upcoming sessions
     */
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<ExpertSessionResponse>> getUpcomingSessionsForUser(
            @PathVariable UUID userId) {

        List<ExpertSession> sessions = expertService.getUpcomingSessionsForUser(userId);

        List<ExpertSessionResponse> response = sessions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Completes a session.
     *
     * @param id the session ID
     * @param notes the completion notes
     * @return the completed session
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ExpertSessionResponse> completeSession(
            @PathVariable UUID id,
            @RequestParam String notes) {

        ExpertSession session = expertService.completeExpertSession(id, notes);

        ExpertSessionResponse response = convertToResponse(session);
        return ResponseEntity.ok(response);
    }

    /**
     * Rates a session.
     *
     * @param id the session ID
     * @param rating the rating (1-5)
     * @param feedback the feedback
     * @param userDetails the authenticated user
     * @return the rated session
     */
    @PostMapping("/{id}/rate")
    public ResponseEntity<ExpertSessionResponse> rateSession(
            @PathVariable UUID id,
            @RequestParam int rating,
            @RequestParam(required = false) String feedback,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        ExpertSession session = expertService.rateExpertSession(id, rating, feedback, userId);

        ExpertSessionResponse response = convertToResponse(session);
        return ResponseEntity.ok(response);
    }

    /**
     * Converts an ExpertSession entity to an ExpertSessionResponse DTO.
     *
     * @param session the session entity
     * @return the session response DTO
     */
    private ExpertSessionResponse convertToResponse(ExpertSession session) {
        ExpertSessionResponse response = new ExpertSessionResponse();
        response.setId(session.getId());
        response.setScheduledAt(session.getScheduledAt());
        response.setEndTime(session.getEndTime());
        response.setDurationMinutes(session.getDurationMinutes());
        response.setStatus(session.getStatus());
        response.setMeetingLink(session.getMeetingLink());
        response.setNotes(session.getNotes());
        response.setUserRating(session.getUserRating());
        response.setUserFeedback(session.getUserFeedback());
        response.setExpertNotes(session.getExpertNotes());
        response.setCancelledReason(session.getCancelledReason());
        response.setCancelledAt(session.getCancelledAt());
        response.setCancelledBy(session.getCancelledBy());

        if (session.getUser() != null) {
            response.setUserId(session.getUser().getId());
            response.setUserName(session.getUser().getDisplayName());
        }

        if (session.getExpert() != null) {
            response.setExpertId(session.getExpert().getId());
            if (session.getExpert().getUserProfile() != null) {
                response.setExpertName(session.getExpert().getUserProfile().getDisplayName());
            }
        }

        if (session.getTicket() != null) {
            response.setTicketId(session.getTicket().getId());
            response.setTicketTitle(session.getTicket().getTitle());
        }

        return response;
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
