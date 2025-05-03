package com.planb.supportticket.service.impl;

import com.planb.supportticket.dto.ConsultationDTO;
import com.planb.supportticket.entity.Consultation;
import com.planb.supportticket.entity.Expert;
import com.planb.supportticket.entity.Ticket;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.ConsultationStatus;
import com.planb.supportticket.exception.ResourceNotFoundException;
import com.planb.supportticket.repository.ConsultationRepository;
import com.planb.supportticket.service.ConsultationService;
import com.planb.supportticket.service.ExpertService;
import com.planb.supportticket.service.NotificationService;
import com.planb.supportticket.service.TicketService;
import com.planb.supportticket.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the ConsultationService interface.
 * Handles scheduling, status changes, and consultation-related operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final UserService userService;
    private final ExpertService expertService;
    private final TicketService ticketService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Consultation scheduleConsultation(UUID userId, UUID expertId, ConsultationDTO consultationDTO) {
        UserProfile user = userService.getUserProfileById(userId);
        Expert expert = expertService.getExpertById(expertId);
        
        Ticket ticket = null;
        if (consultationDTO.getTicketId() != null) {
            ticket = ticketService.getTicketById(consultationDTO.getTicketId());
        }

        Consultation consultation = Consultation.builder()
                .user(user)
                .expert(expert)
                .ticket(ticket)
                .scheduledAt(consultationDTO.getScheduledAt())
                .durationMinutes(consultationDTO.getDurationMinutes())
                .notes(consultationDTO.getNotes())
                .meetingLink(consultationDTO.getMeetingLink())
                .status(ConsultationStatus.SCHEDULED)
                .build();

        Consultation savedConsultation = consultationRepository.save(consultation);

        // Send notification
        notificationService.sendConsultationScheduledNotification(savedConsultation);
        notificationService.sendConsultationConfirmationEmail(savedConsultation);

        return savedConsultation;
    }

    @Override
    public Consultation getConsultationById(UUID id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + id));
    }

    @Override
    @Transactional
    public Consultation updateConsultation(UUID id, ConsultationDTO consultationDTO) {
        Consultation consultation = getConsultationById(id);
        
        consultation.setScheduledAt(consultationDTO.getScheduledAt());
        consultation.setDurationMinutes(consultationDTO.getDurationMinutes());
        consultation.setNotes(consultationDTO.getNotes());
        consultation.setMeetingLink(consultationDTO.getMeetingLink());
        
        if (consultationDTO.getTicketId() != null) {
            Ticket ticket = ticketService.getTicketById(consultationDTO.getTicketId());
            consultation.setTicket(ticket);
        }

        return consultationRepository.save(consultation);
    }

    @Override
    @Transactional
    public Consultation cancelConsultation(UUID id, String reason, UUID userId) {
        Consultation consultation = getConsultationById(id);
        UserProfile user = userService.getUserProfileById(userId);
        
        consultation.cancel(reason, user.getDisplayName());
        Consultation cancelledConsultation = consultationRepository.save(consultation);
        
        // Send notification
        notificationService.sendConsultationCancelledNotification(cancelledConsultation);
        
        return cancelledConsultation;
    }

    @Override
    @Transactional
    public Consultation startConsultation(UUID id) {
        Consultation consultation = getConsultationById(id);
        
        consultation.startConsultation();
        return consultationRepository.save(consultation);
    }

    @Override
    @Transactional
    public Consultation completeConsultation(UUID id, String notes) {
        Consultation consultation = getConsultationById(id);
        
        consultation.complete(notes);
        Consultation completedConsultation = consultationRepository.save(consultation);
        
        // Send notification
        notificationService.sendConsultationCompletedNotification(completedConsultation);
        
        return completedConsultation;
    }

    @Override
    @Transactional
    public Consultation markAsNoShow(UUID id) {
        Consultation consultation = getConsultationById(id);
        
        consultation.updateStatus(ConsultationStatus.NO_SHOW);
        return consultationRepository.save(consultation);
    }

    @Override
    @Transactional
    public Consultation rateConsultation(UUID id, int rating, String feedback, UUID userId) {
        Consultation consultation = getConsultationById(id);
        
        // Verify that the user is the one who participated in the consultation
        if (!consultation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the user who participated in the consultation can rate it");
        }
        
        consultation.rate(rating, feedback);
        
        // Update expert's average rating
        Expert expert = consultation.getExpert();
        int totalRatings = expert.getTotalRatings() != null ? expert.getTotalRatings() : 0;
        double averageRating = expert.getAverageRating() != null ? expert.getAverageRating() : 0.0;
        
        double newAverageRating = ((averageRating * totalRatings) + rating) / (totalRatings + 1);
        expert.setAverageRating(newAverageRating);
        expert.setTotalRatings(totalRatings + 1);
        
        expertService.updateExpert(expert);
        
        return consultationRepository.save(consultation);
    }

    @Override
    public Page<Consultation> getConsultationsByExpertId(UUID expertId, Pageable pageable) {
        return consultationRepository.findByExpertId(expertId, pageable);
    }

    @Override
    public Page<Consultation> getConsultationsByUserId(UUID userId, Pageable pageable) {
        return consultationRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Consultation> getConsultationsByStatus(ConsultationStatus status, Pageable pageable) {
        return consultationRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Consultation> getConsultationsByTicketId(UUID ticketId, Pageable pageable) {
        // This method is not directly supported by the repository, so we'll need to implement it
        // For now, we'll return an empty page
        return Page.empty(pageable);
    }

    @Override
    public List<Consultation> getUpcomingConsultationsForExpert(UUID expertId) {
        return consultationRepository.findUpcomingConsultationsForExpert(expertId, LocalDateTime.now());
    }

    @Override
    public List<Consultation> getUpcomingConsultationsForUser(UUID userId) {
        return consultationRepository.findUpcomingConsultationsForUser(userId, LocalDateTime.now());
    }
}
