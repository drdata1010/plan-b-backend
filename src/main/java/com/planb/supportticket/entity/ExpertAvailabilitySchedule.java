package com.planb.supportticket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Entity representing an expert's availability schedule.
 * Defines when an expert is available for consultations.
 */
@Entity
@Table(name = "expert_availability_schedule", 
       indexes = {
           @Index(name = "idx_availability_expert", columnList = "expert_id"),
           @Index(name = "idx_availability_day", columnList = "day_of_week")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertAvailabilitySchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    @Column(name = "max_consultations")
    private Integer maxConsultations;

    @Column(name = "notes", length = 500)
    private String notes;
    
    /**
     * Checks if a given time is within this availability schedule.
     *
     * @param time the time to check
     * @return true if the time is within this schedule, false otherwise
     */
    public boolean isTimeWithinSchedule(LocalTime time) {
        return isAvailable && 
               !time.isBefore(startTime) && 
               !time.isAfter(endTime);
    }
    
    /**
     * Gets the duration of this availability schedule in minutes.
     *
     * @return the duration in minutes
     */
    public int getDurationMinutes() {
        return (endTime.getHour() * 60 + endTime.getMinute()) - 
               (startTime.getHour() * 60 + startTime.getMinute());
    }
}
