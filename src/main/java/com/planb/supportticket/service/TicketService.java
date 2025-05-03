package com.planb.supportticket.service;

import com.planb.supportticket.dto.TicketDTO;
import com.planb.supportticket.dto.TicketCommentDTO;
import com.planb.supportticket.entity.Attachment;
import com.planb.supportticket.entity.Ticket;
import com.planb.supportticket.entity.TicketComment;
import com.planb.supportticket.entity.enums.TicketStatus;
import com.planb.supportticket.entity.enums.TicketPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for ticket management operations.
 * Handles CRUD operations, status management, assignment logic, comment handling, and attachment processing.
 */
public interface TicketService {

    /**
     * Creates a new ticket.
     *
     * @param ticketDTO the ticket data
     * @param userId the ID of the user creating the ticket
     * @return the created ticket
     */
    Ticket createTicket(TicketDTO ticketDTO, UUID userId);

    /**
     * Gets a ticket by ID.
     *
     * @param id the ticket ID
     * @return the ticket
     */
    Ticket getTicketById(UUID id);

    /**
     * Gets a ticket by its ticket number.
     *
     * @param ticketNumber the ticket number (e.g., "TK-1")
     * @return the ticket
     */
    Ticket getTicketByNumber(String ticketNumber);

    /**
     * Updates a ticket.
     *
     * @param id the ticket ID
     * @param ticketDTO the updated ticket data
     * @return the updated ticket
     */
    Ticket updateTicket(UUID id, TicketDTO ticketDTO);

    /**
     * Updates a ticket by its ticket number.
     *
     * @param ticketNumber the ticket number (e.g., "TK-1")
     * @param ticketDTO the updated ticket data
     * @return the updated ticket
     */
    Ticket updateTicketByNumber(String ticketNumber, TicketDTO ticketDTO);

    /**
     * Deletes a ticket.
     *
     * @param id the ticket ID
     */
    void deleteTicket(UUID id);

    /**
     * Gets all tickets with pagination.
     *
     * @param pageable the pagination information
     * @return a page of tickets
     */
    Page<Ticket> getAllTickets(Pageable pageable);

    /**
     * Gets all tickets ordered by creation date in descending order (newest first) with pagination.
     *
     * @param pageable the pagination information
     * @return a page of tickets ordered by creation date in descending order
     */
    Page<Ticket> getAllTicketsOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Gets tickets by user ID with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of tickets
     */
    Page<Ticket> getTicketsByUserId(UUID userId, Pageable pageable);

    /**
     * Gets tickets by user ID ordered by creation date in descending order (newest first) with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of tickets ordered by creation date in descending order
     */
    Page<Ticket> getTicketsByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Gets tickets by assigned expert ID with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of tickets
     */
    Page<Ticket> getTicketsByExpertId(UUID expertId, Pageable pageable);

    /**
     * Gets tickets by assigned expert ID ordered by creation date in descending order (newest first) with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of tickets ordered by creation date in descending order
     */
    Page<Ticket> getTicketsByExpertIdOrderByCreatedAtDesc(UUID expertId, Pageable pageable);

    /**
     * Gets tickets by status with pagination.
     *
     * @param status the ticket status
     * @param pageable the pagination information
     * @return a page of tickets
     */
    Page<Ticket> getTicketsByStatus(TicketStatus status, Pageable pageable);

    /**
     * Gets tickets by status ordered by creation date in descending order (newest first) with pagination.
     *
     * @param status the ticket status
     * @param pageable the pagination information
     * @return a page of tickets ordered by creation date in descending order
     */
    Page<Ticket> getTicketsByStatusOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);

    /**
     * Gets tickets by priority with pagination.
     *
     * @param priority the ticket priority
     * @param pageable the pagination information
     * @return a page of tickets
     */
    Page<Ticket> getTicketsByPriority(TicketPriority priority, Pageable pageable);

    /**
     * Gets tickets by priority ordered by creation date in descending order (newest first) with pagination.
     *
     * @param priority the ticket priority
     * @param pageable the pagination information
     * @return a page of tickets ordered by creation date in descending order
     */
    Page<Ticket> getTicketsByPriorityOrderByCreatedAtDesc(TicketPriority priority, Pageable pageable);

    /**
     * Updates the status of a ticket.
     *
     * @param id the ticket ID
     * @param status the new status
     * @return the updated ticket
     */
    Ticket updateTicketStatus(UUID id, TicketStatus status);

    /**
     * Updates the priority of a ticket.
     *
     * @param id the ticket ID
     * @param priority the new priority
     * @return the updated ticket
     */
    Ticket updateTicketPriority(UUID id, TicketPriority priority);

    /**
     * Assigns a ticket to an expert.
     *
     * @param ticketId the ticket ID
     * @param expertId the expert ID
     * @return the updated ticket
     */
    Ticket assignTicketToExpert(UUID ticketId, UUID expertId);

    /**
     * Unassigns a ticket from an expert.
     *
     * @param ticketId the ticket ID
     * @return the updated ticket
     */
    Ticket unassignTicket(UUID ticketId);

    /**
     * Adds a comment to a ticket.
     *
     * @param ticketId the ticket ID
     * @param commentDTO the comment data
     * @param userId the ID of the user adding the comment
     * @return the created comment
     */
    TicketComment addComment(UUID ticketId, TicketCommentDTO commentDTO, UUID userId);

    /**
     * Gets comments for a ticket.
     *
     * @param ticketId the ticket ID
     * @return a list of comments
     */
    List<TicketComment> getComments(UUID ticketId);

    /**
     * Gets a comment by ID.
     *
     * @param commentId the comment ID
     * @return the comment
     */
    TicketComment getCommentById(UUID commentId);

    /**
     * Updates a comment.
     *
     * @param commentId the comment ID
     * @param commentDTO the updated comment data
     * @return the updated comment
     */
    TicketComment updateComment(UUID commentId, TicketCommentDTO commentDTO);

    /**
     * Deletes a comment.
     *
     * @param commentId the comment ID
     */
    void deleteComment(UUID commentId);

    /**
     * Adds a reply to a comment.
     *
     * @param parentCommentId the parent comment ID
     * @param commentDTO the reply data
     * @param userId the ID of the user adding the reply
     * @return the created reply
     */
    TicketComment addReply(UUID parentCommentId, TicketCommentDTO commentDTO, UUID userId);

    /**
     * Adds an attachment to a ticket.
     *
     * @param ticketId the ticket ID
     * @param file the file to attach
     * @param userId the ID of the user adding the attachment
     * @return the created attachment
     */
    Attachment addAttachment(UUID ticketId, MultipartFile file, UUID userId);

    /**
     * Gets attachments for a ticket.
     *
     * @param ticketId the ticket ID
     * @return a list of attachments
     */
    List<Attachment> getAttachments(UUID ticketId);

    /**
     * Gets an attachment by ID.
     *
     * @param attachmentId the attachment ID
     * @return the attachment
     */
    Attachment getAttachmentById(UUID attachmentId);

    /**
     * Deletes an attachment.
     *
     * @param attachmentId the attachment ID
     */
    void deleteAttachment(UUID attachmentId);

    /**
     * Adds an attachment to a comment.
     *
     * @param commentId the comment ID
     * @param file the file to attach
     * @param userId the ID of the user adding the attachment
     * @return the created attachment
     */
    Attachment addCommentAttachment(UUID commentId, MultipartFile file, UUID userId);

    /**
     * Gets attachments for a comment.
     *
     * @param commentId the comment ID
     * @return a list of attachments
     */
    List<Attachment> getCommentAttachments(UUID commentId);

    /**
     * Resolves a ticket.
     *
     * @param ticketId the ticket ID
     * @param resolution the resolution message
     * @param userId the ID of the user resolving the ticket
     * @return the updated ticket
     */
    Ticket resolveTicket(UUID ticketId, String resolution, UUID userId);

    /**
     * Reopens a ticket.
     *
     * @param ticketId the ticket ID
     * @param reason the reason for reopening
     * @param userId the ID of the user reopening the ticket
     * @return the updated ticket
     */
    Ticket reopenTicket(UUID ticketId, String reason, UUID userId);

    /**
     * Searches for tickets by keyword.
     *
     * @param keyword the search keyword
     * @param pageable the pagination information
     * @return a page of tickets
     */
    Page<Ticket> searchTickets(String keyword, Pageable pageable);

    /**
     * Updates a ticket by its ticket number with attachments.
     *
     * @param ticketNumber the ticket number (e.g., "TK-1")
     * @param ticketData the ticket data as form fields
     * @param attachments the files to attach to the ticket
     * @param userId the ID of the user updating the ticket
     * @return the updated ticket
     */
    Ticket updateTicketWithAttachments(String ticketNumber, TicketDTO ticketData, List<MultipartFile> attachments, UUID userId);
}
