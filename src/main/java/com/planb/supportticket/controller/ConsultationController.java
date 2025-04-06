package com.planb.supportticket.controller;

import com.planb.supportticket.dto.ConsultationDTO;
import com.planb.supportticket.dto.ConsultationResponse;
import com.planb.supportticket.entity.Consultation;
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
 * Controller for consultation operations.
 * Handles consultation scheduling, management, and feedback.
 */
@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@Slf4j
public class ConsultationController {
    private final ExpertService expertService;
    
    /**
     * Schedules a consultation with an expert.
     *
     * @param expertId the expert ID
     * @param consultationDTO the consultation data
     * @param userDetails the authenticated user
     * @return the scheduled consultation
     */
    @PostMapping("/expert/{expertId}")
    public ResponseEntity<ConsultationResponse> scheduleConsultation(
            @PathVariable UUID expertId,
            @Valid @RequestBody ConsultationDTO consultationDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = getUserIdFromUserDetails(userDetails);
        Consultation consultation = expertService.scheduleConsultation(expertId, consultationDTO, userId);
        
        ConsultationResponse response = convertToResponse(consultation);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Gets a consultation by ID.
     *
     * @param id the consultation ID
     * @return the consultation
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponse> getConsultation(@PathVariable UUID id) {
        Consultation consultation = expertService.getConsultationById(id);
        
        ConsultationResponse response = convertToResponse(consultation);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Updates a consultation.
     *
     * @param id the consultation ID
     * @param consultationDTO the updated consultation data
     * @return the updated consultation
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConsultationResponse> updateConsultation(
            @PathVariable UUID id,
            @Valid @RequestBody ConsultationDTO consultationDTO) {
        
        Consultation consultation = expertService.updateConsultation(id, consultationDTO);
        
        ConsultationResponse response = convertToResponse(consultation);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancels a consultation.
     *
     * @param id the consultation ID
     * @param reason the cancellation reason
     * @param userDetails the authenticated user
     * @return the cancelled consultation
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ConsultationResponse> cancelConsultation(
            @PathVariable UUID id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = getUserIdFromUserDetails(userDetails);
        Consultation consultation = expertService.cancelConsultation(id, reason, userId);
        
        ConsultationResponse response = convertToResponse(consultation);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets consultations for an expert with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of consultations
     */
    @GetMapping("/expert/{expertId}")
    public ResponseEntity<Page<ConsultationResponse>> getConsultationsByExpertId(
            @PathVariable UUID expertId,
            Pageable pageable) {
        
        Page<Consultation> consultations = expertService.getConsultationsByExpertId(expertId, pageable);
        
        Page<ConsultationResponse> response = consultations.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets consultations for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of consultations
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ConsultationResponse>> getConsultationsByUserId(
            @PathVariable UUID userId,
            Pageable pageable) {
        
        Page<Consultation> consultations = expertService.getConsultationsByUserId(userId, pageable);
        
        Page<ConsultationResponse> response = consultations.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets upcoming consultations for an expert.
     *
     * @param expertId the expert ID
     * @return a list of upcoming consultations
     */
    @GetMapping("/expert/{expertId}/upcoming")
    public ResponseEntity<List<ConsultationResponse>> getUpcomingConsultationsForExpert(
            @PathVariable UUID expertId) {
        
        List<Consultation> consultations = expertService.getUpcomingConsultationsForExpert(expertId);
        
        List<ConsultationResponse> response = consultations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets upcoming consultations for a user.
     *
     * @param userId the user ID
     * @return a list of upcoming consultations
     */
    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<ConsultationResponse>> getUpcomingConsultationsForUser(
            @PathVariable UUID userId) {
        
        List<Consultation> consultations = expertService.getUpcomingConsultationsForUser(userId);
        
        List<ConsultationResponse> response = consultations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Completes a consultation.
     *
     * @param id the consultation ID
     * @param notes the completion notes
     * @return the completed consultation
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ConsultationResponse> completeConsultation(
            @PathVariable UUID id,
            @RequestParam String notes) {
        
        Consultation consultation = expertService.completeConsultation(id, notes);
        
        ConsultationResponse response = convertToResponse(consultation);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Rates a consultation.
     *
     * @param id the consultation ID
     * @param rating the rating (1-5)
     * @param feedback the feedback
     * @param userDetails the authenticated user
     * @return the rated consultation
     */
    @PostMapping("/{id}/rate")
    public ResponseEntity<ConsultationResponse> rateConsultation(
            @PathVariable UUID id,
            @RequestParam int rating,
            @RequestParam(required = false) String feedback,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = getUserIdFromUserDetails(userDetails);
        Consultation consultation = expertService.rateConsultation(id, rating, feedback, userId);
        
        ConsultationResponse response = convertToResponse(consultation);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Converts a Consultation entity to a ConsultationResponse DTO.
     *
     * @param consultation the consultation entity
     * @return the consultation response DTO
     */
    private ConsultationResponse convertToResponse(Consultation consultation) {
        ConsultationResponse response = new ConsultationResponse();
        response.setId(consultation.getId());
        response.setScheduledAt(consultation.getScheduledAt());
        response.setEndTime(consultation.getEndTime());
        response.setDurationMinutes(consultation.getDurationMinutes());
        response.setStatus(consultation.getStatus());
        response.setMeetingLink(consultation.getMeetingLink());
        response.setNotes(consultation.getNotes());
        response.setUserRating(consultation.getUserRating());
        response.setUserFeedback(consultation.getUserFeedback());
        response.setExpertNotes(consultation.getExpertNotes());
        response.setCancelledReason(consultation.getCancelledReason());
        response.setCancelledAt(consultation.getCancelledAt());
        response.setCancelledBy(consultation.getCancelledBy());
        
        if (consultation.getUser() != null) {
            response.setUserId(consultation.getUser().getId());
            response.setUserName(consultation.getUser().getDisplayName());
        }
        
        if (consultation.getExpert() != null) {
            response.setExpertId(consultation.getExpert().getId());
            if (consultation.getExpert().getUserProfile() != null) {
                response.setExpertName(consultation.getExpert().getUserProfile().getDisplayName());
            }
        }
        
        if (consultation.getTicket() != null) {
            response.setTicketId(consultation.getTicket().getId());
            response.setTicketTitle(consultation.getTicket().getTitle());
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
