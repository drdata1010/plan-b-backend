package com.planb.supportticket.service.impl;

import com.planb.supportticket.config.aws.S3Service;
import com.planb.supportticket.dto.TicketDTO;
import com.planb.supportticket.dto.TicketCommentDTO;
import com.planb.supportticket.entity.*;
import com.planb.supportticket.entity.enums.TicketStatus;
import com.planb.supportticket.entity.enums.TicketPriority;
import com.planb.supportticket.exception.ResourceNotFoundException;
import com.planb.supportticket.exception.UnauthorizedException;
import com.planb.supportticket.repository.*;
import com.planb.supportticket.service.NotificationService;
import com.planb.supportticket.service.TicketNumberService;
import com.planb.supportticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the TicketService interface.
 * Handles CRUD operations, status management, assignment logic, comment handling, and attachment processing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserProfileRepository userProfileRepository;
    private final ExpertRepository expertRepository;
    private final TicketCommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final S3Service s3Service;
    private final NotificationService notificationService;
    private final TicketNumberService ticketNumberService;

    @Override
    public Ticket createTicket(TicketDTO ticketDTO, UUID userId) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(ticketNumberService.generateTicketNumber());
        ticket.setTitle(ticketDTO.getTitle());
        ticket.setDescription(ticketDTO.getDescription());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(ticketDTO.getPriority() != null ? ticketDTO.getPriority() : TicketPriority.MEDIUM);
        ticket.setClassification(ticketDTO.getClassification());
        ticket.setArea(ticketDTO.getArea());
        ticket.setUser(user);

        if (ticketDTO.getDueDate() != null) {
            ticket.setDueDate(ticketDTO.getDueDate());
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        // Send notification
        notificationService.sendTicketCreatedNotification(savedTicket);

        return savedTicket;
    }

    @Override
    public Ticket getTicketById(UUID id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    @Override
    public Ticket getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));
    }

    @Override
    public Ticket updateTicket(UUID id, TicketDTO ticketDTO) {
        Ticket ticket = getTicketById(id);

        if (ticketDTO.getTitle() != null) {
            ticket.setTitle(ticketDTO.getTitle());
        }
        if (ticketDTO.getDescription() != null) {
            ticket.setDescription(ticketDTO.getDescription());
        }
        if (ticketDTO.getPriority() != null) {
            ticket.setPriority(ticketDTO.getPriority());
        }
        if (ticketDTO.getClassification() != null) {
            ticket.setClassification(ticketDTO.getClassification());
        }
        if (ticketDTO.getArea() != null) {
            ticket.setArea(ticketDTO.getArea());
        }
        if (ticketDTO.getDueDate() != null) {
            ticket.setDueDate(ticketDTO.getDueDate());
        }

        Ticket updatedTicket = ticketRepository.save(ticket);

        // Send notification
        notificationService.sendTicketUpdatedNotification(updatedTicket);

        return updatedTicket;
    }

    @Override
    public void deleteTicket(UUID id) {
        Ticket ticket = getTicketById(id);

        // Delete attachments from S3
        for (Attachment attachment : ticket.getAttachments()) {
            try {
                s3Service.deleteFile(attachment.getS3Key());
            } catch (Exception e) {
                log.error("Error deleting attachment from S3: {}", e.getMessage());
                // Continue with deletion even if S3 deletion fails
            }
        }

        ticketRepository.delete(ticket);
    }

    @Override
    public Page<Ticket> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    @Override
    public Page<Ticket> getAllTicketsOrderByCreatedAtDesc(Pageable pageable) {
        return ticketRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<Ticket> getTicketsByUserId(UUID userId, Pageable pageable) {
        return ticketRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Ticket> getTicketsByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Page<Ticket> getTicketsByExpertId(UUID expertId, Pageable pageable) {
        return ticketRepository.findByAssignedExpertId(expertId, pageable);
    }

    @Override
    public Page<Ticket> getTicketsByExpertIdOrderByCreatedAtDesc(UUID expertId, Pageable pageable) {
        return ticketRepository.findByAssignedExpertIdOrderByCreatedAtDesc(expertId, pageable);
    }

    @Override
    public Page<Ticket> getTicketsByStatus(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Ticket> getTicketsByStatusOrderByCreatedAtDesc(TicketStatus status, Pageable pageable) {
        return ticketRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    public Page<Ticket> getTicketsByPriority(TicketPriority priority, Pageable pageable) {
        return ticketRepository.findByPriority(priority, pageable);
    }

    @Override
    public Page<Ticket> getTicketsByPriorityOrderByCreatedAtDesc(TicketPriority priority, Pageable pageable) {
        return ticketRepository.findByPriorityOrderByCreatedAtDesc(priority, pageable);
    }

    @Override
    public Ticket updateTicketStatus(UUID id, TicketStatus status) {
        Ticket ticket = getTicketById(id);
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(status);

        // Set resolved or closed time if applicable
        if (status == TicketStatus.RESOLVED && ticket.getResolvedAt() == null) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if (status == TicketStatus.CLOSED && ticket.getClosedAt() == null) {
            ticket.setClosedAt(LocalDateTime.now());
        }

        Ticket updatedTicket = ticketRepository.save(ticket);

        // Send notification
        notificationService.sendTicketStatusChangedNotification(updatedTicket, oldStatus);

        return updatedTicket;
    }

    @Override
    public Ticket updateTicketPriority(UUID id, TicketPriority priority) {
        Ticket ticket = getTicketById(id);
        ticket.setPriority(priority);

        Ticket updatedTicket = ticketRepository.save(ticket);

        // Send notification
        notificationService.sendTicketPriorityChangedNotification(updatedTicket);

        return updatedTicket;
    }

    @Override
    public Ticket assignTicketToExpert(UUID ticketId, UUID expertId) {
        Ticket ticket = getTicketById(ticketId);
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new ResourceNotFoundException("Expert not found with id: " + expertId));

        ticket.setAssignedExpert(expert);

        Ticket updatedTicket = ticketRepository.save(ticket);

        // Send notification
        notificationService.sendTicketAssignedNotification(updatedTicket);

        return updatedTicket;
    }

    @Override
    public Ticket unassignTicket(UUID ticketId) {
        Ticket ticket = getTicketById(ticketId);

        if (ticket.getAssignedExpert() != null) {
            Expert previousExpert = ticket.getAssignedExpert();
            ticket.setAssignedExpert(null);

            Ticket updatedTicket = ticketRepository.save(ticket);

            // Send notification
            notificationService.sendTicketUnassignedNotification(updatedTicket, previousExpert);

            return updatedTicket;
        }

        return ticket;
    }

    @Override
    public TicketComment addComment(UUID ticketId, TicketCommentDTO commentDTO, UUID userId) {
        Ticket ticket = getTicketById(ticketId);
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        TicketComment comment = new TicketComment();
        comment.setContent(commentDTO.getContent());
        comment.setTicket(ticket);
        comment.setUser(user);
        comment.setInternalNote(commentDTO.isInternalNote());

        TicketComment savedComment = commentRepository.save(comment);

        // Add comment to ticket
        ticket.addComment(savedComment);

        // Send notification
        notificationService.sendCommentAddedNotification(savedComment);

        return savedComment;
    }

    @Override
    public List<TicketComment> getComments(UUID ticketId) {
        Ticket ticket = getTicketById(ticketId);
        return ticket.getComments();
    }

    @Override
    public TicketComment getCommentById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
    }

    @Override
    public TicketComment updateComment(UUID commentId, TicketCommentDTO commentDTO) {
        TicketComment comment = getCommentById(commentId);

        comment.setContent(commentDTO.getContent());
        comment.setInternalNote(commentDTO.isInternalNote());

        return commentRepository.save(comment);
    }

    @Override
    public void deleteComment(UUID commentId) {
        TicketComment comment = getCommentById(commentId);

        // Delete attachments from S3
        for (Attachment attachment : comment.getAttachments()) {
            try {
                s3Service.deleteFile(attachment.getS3Key());
            } catch (Exception e) {
                log.error("Error deleting attachment from S3: {}", e.getMessage());
                // Continue with deletion even if S3 deletion fails
            }
        }

        commentRepository.delete(comment);
    }

    @Override
    public TicketComment addReply(UUID parentCommentId, TicketCommentDTO commentDTO, UUID userId) {
        TicketComment parentComment = getCommentById(parentCommentId);
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        TicketComment reply = new TicketComment();
        reply.setContent(commentDTO.getContent());
        reply.setTicket(parentComment.getTicket());
        reply.setUser(user);
        reply.setParent(parentComment);
        reply.setInternalNote(commentDTO.isInternalNote());

        TicketComment savedReply = commentRepository.save(reply);

        // Add reply to parent comment
        parentComment.addReply(savedReply);

        // Send notification
        notificationService.sendReplyAddedNotification(savedReply);

        return savedReply;
    }

    @Override
    public Attachment addAttachment(UUID ticketId, MultipartFile file, UUID userId) {
        Ticket ticket = getTicketById(ticketId);
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        try {
            // Upload file to S3
            String s3Key = s3Service.uploadFile(file, "tickets/" + ticketId);

            // Create attachment
            Attachment attachment = new Attachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setS3Key(s3Key);
            attachment.setUser(user);
            attachment.setTicket(ticket);
            attachment.setPublic(true);

            Attachment savedAttachment = attachmentRepository.save(attachment);

            // Add attachment to ticket
            ticket.addAttachment(savedAttachment);

            return savedAttachment;
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage());
            throw new RuntimeException("Error uploading file", e);
        }
    }

    @Override
    public List<Attachment> getAttachments(UUID ticketId) {
        Ticket ticket = getTicketById(ticketId);
        return ticket.getAttachments();
    }

    @Override
    public Attachment getAttachmentById(UUID attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with id: " + attachmentId));
    }

    @Override
    public void deleteAttachment(UUID attachmentId) {
        Attachment attachment = getAttachmentById(attachmentId);

        // Delete from S3
        try {
            s3Service.deleteFile(attachment.getS3Key());
        } catch (Exception e) {
            log.error("Error deleting attachment from S3: {}", e.getMessage());
            // Continue with deletion even if S3 deletion fails
        }

        attachmentRepository.delete(attachment);
    }

    @Override
    public Attachment addCommentAttachment(UUID commentId, MultipartFile file, UUID userId) {
        TicketComment comment = getCommentById(commentId);
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        try {
            // Upload file to S3
            String s3Key = s3Service.uploadFile(file, "comments/" + commentId);

            // Create attachment
            Attachment attachment = new Attachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setS3Key(s3Key);
            attachment.setUser(user);
            attachment.setComment(comment);
            attachment.setPublic(true);

            Attachment savedAttachment = attachmentRepository.save(attachment);

            // Add attachment to comment
            comment.addAttachment(savedAttachment);

            return savedAttachment;
        } catch (Exception e) {
            log.error("Error uploading file to S3: {}", e.getMessage());
            throw new RuntimeException("Error uploading file", e);
        }
    }

    @Override
    public List<Attachment> getCommentAttachments(UUID commentId) {
        TicketComment comment = getCommentById(commentId);
        return comment.getAttachments();
    }

    @Override
    public Ticket resolveTicket(UUID ticketId, String resolution, UUID userId) {
        Ticket ticket = getTicketById(ticketId);
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user is authorized to resolve the ticket
        boolean isAssignedExpert = ticket.getAssignedExpert() != null &&
                ticket.getAssignedExpert().getUserProfile().getId().equals(userId);
        boolean isTicketOwner = ticket.getUser().getId().equals(userId);

        if (!isAssignedExpert && !isTicketOwner && !isAdmin(user)) {
            throw new UnauthorizedException("You are not authorized to resolve this ticket");
        }

        // Add resolution comment
        TicketCommentDTO commentDTO = new TicketCommentDTO();
        commentDTO.setContent("Resolution: " + resolution);
        commentDTO.setInternalNote(false);
        addComment(ticketId, commentDTO, userId);

        // Update ticket status
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(LocalDateTime.now());

        Ticket resolvedTicket = ticketRepository.save(ticket);

        // Send notification
        notificationService.sendTicketResolvedNotification(resolvedTicket);

        return resolvedTicket;
    }

    @Override
    public Ticket reopenTicket(UUID ticketId, String reason, UUID userId) {
        Ticket ticket = getTicketById(ticketId);
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if ticket is resolved or closed
        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            throw new IllegalStateException("Ticket is not resolved or closed");
        }

        // Check if user is authorized to reopen the ticket
        boolean isTicketOwner = ticket.getUser().getId().equals(userId);

        if (!isTicketOwner && !isAdmin(user)) {
            throw new UnauthorizedException("You are not authorized to reopen this ticket");
        }

        // Add reopen comment
        TicketCommentDTO commentDTO = new TicketCommentDTO();
        commentDTO.setContent("Ticket reopened: " + reason);
        commentDTO.setInternalNote(false);
        addComment(ticketId, commentDTO, userId);

        // Update ticket status
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setResolvedAt(null);
        ticket.setClosedAt(null);

        Ticket reopenedTicket = ticketRepository.save(ticket);

        // Send notification
        notificationService.sendTicketReopenedNotification(reopenedTicket);

        return reopenedTicket;
    }

    @Override
    public Page<Ticket> searchTickets(String keyword, Pageable pageable) {
        return ticketRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword, pageable);
    }

    /**
     * Checks if a user has admin role.
     *
     * @param user the user to check
     * @return true if the user has admin role, false otherwise
     */
    private boolean isAdmin(UserProfile user) {
        return user.getRoles().contains("ROLE_ADMIN");
    }
}
