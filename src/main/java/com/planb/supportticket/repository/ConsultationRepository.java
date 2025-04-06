package com.planb.supportticket.repository;

import com.planb.supportticket.entity.Consultation;
import com.planb.supportticket.entity.enums.ConsultationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Consultation entities.
 */
@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
    
    /**
     * Finds consultations by expert ID with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of consultations
     */
    Page<Consultation> findByExpertId(UUID expertId, Pageable pageable);
    
    /**
     * Finds consultations by user ID with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of consultations
     */
    Page<Consultation> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Finds consultations by status with pagination.
     *
     * @param status the consultation status
     * @param pageable the pagination information
     * @return a page of consultations
     */
    Page<Consultation> findByStatus(ConsultationStatus status, Pageable pageable);
    
    /**
     * Finds upcoming consultations for an expert.
     *
     * @param expertId the expert ID
     * @param now the current date and time
     * @return a list of upcoming consultations
     */
    @Query("SELECT c FROM Consultation c WHERE c.expert.id = :expertId AND c.scheduledAt > :now AND c.status = 'SCHEDULED' ORDER BY c.scheduledAt ASC")
    List<Consultation> findUpcomingConsultationsForExpert(UUID expertId, LocalDateTime now);
    
    /**
     * Finds upcoming consultations for a user.
     *
     * @param userId the user ID
     * @param now the current date and time
     * @return a list of upcoming consultations
     */
    @Query("SELECT c FROM Consultation c WHERE c.user.id = :userId AND c.scheduledAt > :now AND c.status = 'SCHEDULED' ORDER BY c.scheduledAt ASC")
    List<Consultation> findUpcomingConsultationsForUser(UUID userId, LocalDateTime now);
}
