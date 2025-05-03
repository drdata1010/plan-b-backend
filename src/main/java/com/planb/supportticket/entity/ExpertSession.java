package com.planb.supportticket.entity;

import com.planb.supportticket.entity.enums.ExpertSessionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "expert_sessions",
       indexes = {
           @Index(name = "idx_expert_session_user", columnList = "user_id"),
           @Index(name = "idx_expert_session_expert", columnList = "expert_id"),
           @Index(name = "idx_expert_session_ticket", columnList = "ticket_id"),
           @Index(name = "idx_expert_session_status", columnList = "status"),
           @Index(name = "idx_expert_session_scheduled_at", columnList = "scheduled_at")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertSession extends BaseEntity {

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
    private ExpertSessionStatus status;

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

    @OneToOne(mappedBy = "expertSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private ChatSession chatSession;

    /**
     * Creates a chat session for this expert session.
     *
     * @return the created chat session
     */
    public ChatSession createChatSession() {
        if (this.chatSession != null) {
            return this.chatSession;
        }

        ChatSession session = ChatSession.builder()
                .user(this.user)
                .expert(this.expert)
                .ticket(this.ticket)
                .title("Expert Session Chat")
                .startedAt(LocalDateTime.now())
                .isActive(true)
                .sessionType(ChatSession.ChatSessionType.EXPERT_SESSION)
                .build();

        this.chatSession = session;
        session.setExpertSession(this);

        return session;
    }
}
