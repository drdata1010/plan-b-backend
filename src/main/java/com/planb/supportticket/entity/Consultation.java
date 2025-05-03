package com.planb.supportticket.entity;

import com.planb.supportticket.entity.enums.ConsultationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a consultation between a user and an expert.
 * Consultations can be related to tickets and have various statuses.
 */
@Entity
@Table(name = "consultations",
       indexes = {
           @Index(name = "idx_consultation_user", columnList = "user_id"),
           @Index(name = "idx_consultation_expert", columnList = "expert_id"),
           @Index(name = "idx_consultation_ticket", columnList = "ticket_id"),
           @Index(name = "idx_consultation_status", columnList = "status"),
           @Index(name = "idx_consultation_scheduled_at", columnList = "scheduled_at")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConsultationStatus status;

    @Column(name = "meeting_link")
    private String meetingLink;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "user_rating")
    private Integer userRating;

    @Column(name = "user_feedback", length = 1000)
    private String userFeedback;

    @Column(name = "expert_notes", length = 1000)
    private String expertNotes;

    @Column(name = "cancelled_reason", length = 500)
    private String cancelledReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    private String cancelledBy;

    /**
     * Updates the status of this consultation.
     *
     * @param newStatus the new status
     */
    public void updateStatus(ConsultationStatus newStatus) {
        ConsultationStatus oldStatus = this.status;
        this.status = newStatus;

        // Set end time if completed
        if (newStatus == ConsultationStatus.COMPLETED && this.endTime == null) {
            this.endTime = LocalDateTime.now();
        }

        // Set cancelled info if cancelled
        if (newStatus == ConsultationStatus.CANCELLED && this.cancelledAt == null) {
            this.cancelledAt = LocalDateTime.now();
        }
    }

    /**
     * Cancels this consultation.
     *
     * @param reason the cancellation reason
     * @param cancelledBy the user who cancelled the consultation
     */
    public void cancel(String reason, String cancelledBy) {
        this.status = ConsultationStatus.CANCELLED;
        this.cancelledReason = reason;
        this.cancelledAt = LocalDateTime.now();
        this.cancelledBy = cancelledBy;
    }

    /**
     * Marks this consultation as completed.
     *
     * @param expertNotes the expert's notes
     */
    public void complete(String expertNotes) {
        this.status = ConsultationStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.expertNotes = expertNotes;
    }

    /**
     * Marks this consultation as in progress.
     */
    public void startConsultation() {
        this.status = ConsultationStatus.IN_PROGRESS;
    }

    /**
     * Rates this consultation.
     *
     * @param rating the rating (1-5)
     * @param feedback the user's feedback
     */
    public void rate(Integer rating, String feedback) {
        this.userRating = rating;
        this.userFeedback = feedback;
    }

    /**
     * Gets the display name of the user.
     *
     * @return the user's display name
     */
    public String getUserDisplayName() {
        return user != null ? user.getDisplayName() : null;
    }

    /**
     * Gets the display name of the expert.
     *
     * @return the expert's display name
     */
    public String getExpertDisplayName() {
        return expert != null && expert.getUserProfile() != null ? 
               expert.getUserProfile().getDisplayName() : null;
    }
}
