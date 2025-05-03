package com.planb.supportticket.service;

import com.planb.supportticket.dto.ConsultationDTO;
import com.planb.supportticket.entity.Consultation;
import com.planb.supportticket.entity.enums.ConsultationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for consultation management operations.
 * Handles scheduling, status changes, and consultation-related operations.
 */
public interface ConsultationService {

    /**
     * Schedules a consultation between a user and an expert.
     *
     * @param userId the user ID
     * @param expertId the expert ID
     * @param consultationDTO the consultation data
     * @return the scheduled consultation
     */
    Consultation scheduleConsultation(UUID userId, UUID expertId, ConsultationDTO consultationDTO);

    /**
     * Gets a consultation by ID.
     *
     * @param id the consultation ID
     * @return the consultation
     */
    Consultation getConsultationById(UUID id);

    /**
     * Updates a consultation.
     *
     * @param id the consultation ID
     * @param consultationDTO the updated consultation data
     * @return the updated consultation
     */
    Consultation updateConsultation(UUID id, ConsultationDTO consultationDTO);

    /**
     * Cancels a consultation.
     *
     * @param id the consultation ID
     * @param reason the cancellation reason
     * @param userId the ID of the user cancelling the consultation
     * @return the cancelled consultation
     */
    Consultation cancelConsultation(UUID id, String reason, UUID userId);

    /**
     * Starts a consultation.
     *
     * @param id the consultation ID
     * @return the started consultation
     */
    Consultation startConsultation(UUID id);

    /**
     * Completes a consultation.
     *
     * @param id the consultation ID
     * @param notes the completion notes
     * @return the completed consultation
     */
    Consultation completeConsultation(UUID id, String notes);

    /**
     * Marks a consultation as no-show.
     *
     * @param id the consultation ID
     * @return the updated consultation
     */
    Consultation markAsNoShow(UUID id);

    /**
     * Rates a consultation.
     *
     * @param id the consultation ID
     * @param rating the rating (1-5)
     * @param feedback the feedback
     * @param userId the ID of the user rating the consultation
     * @return the rated consultation
     */
    Consultation rateConsultation(UUID id, int rating, String feedback, UUID userId);

    /**
     * Gets consultations for an expert with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of consultations
     */
    Page<Consultation> getConsultationsByExpertId(UUID expertId, Pageable pageable);

    /**
     * Gets consultations for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of consultations
     */
    Page<Consultation> getConsultationsByUserId(UUID userId, Pageable pageable);

    /**
     * Gets consultations by status with pagination.
     *
     * @param status the consultation status
     * @param pageable the pagination information
     * @return a page of consultations
     */
    Page<Consultation> getConsultationsByStatus(ConsultationStatus status, Pageable pageable);

    /**
     * Gets consultations for a ticket with pagination.
     *
     * @param ticketId the ticket ID
     * @param pageable the pagination information
     * @return a page of consultations
     */
    Page<Consultation> getConsultationsByTicketId(UUID ticketId, Pageable pageable);

    /**
     * Gets upcoming consultations for an expert.
     *
     * @param expertId the expert ID
     * @return a list of upcoming consultations
     */
    List<Consultation> getUpcomingConsultationsForExpert(UUID expertId);

    /**
     * Gets upcoming consultations for a user.
     *
     * @param userId the user ID
     * @return a list of upcoming consultations
     */
    List<Consultation> getUpcomingConsultationsForUser(UUID userId);
}
