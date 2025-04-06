package com.planb.supportticket.repository;

import com.planb.supportticket.entity.Ticket;
import com.planb.supportticket.entity.enums.TicketPriority;
import com.planb.supportticket.entity.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for Ticket entities.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    
    /**
     * Finds tickets by user ID with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of tickets
     */
    Page<Ticket> findByUserId(UUID userId, Pageable pageable);
    
    /**
     * Finds tickets by assigned expert ID with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of tickets
     */
    Page<Ticket> findByAssignedExpertId(UUID expertId, Pageable pageable);
    
    /**
     * Finds tickets by status with pagination.
     *
     * @param status the ticket status
     * @param pageable the pagination information
     * @return a page of tickets
     */
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
    
    /**
     * Finds tickets by priority with pagination.
     *
     * @param priority the ticket priority
     * @param pageable the pagination information
     * @return a page of tickets
     */
    Page<Ticket> findByPriority(TicketPriority priority, Pageable pageable);
    
    /**
     * Searches for tickets by title or description with pagination.
     *
     * @param title the title to search for
     * @param description the description to search for
     * @param pageable the pagination information
     * @return a page of tickets
     */
    Page<Ticket> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String title, String description, Pageable pageable);
}
