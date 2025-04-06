package com.planb.supportticket.entity;

import com.planb.supportticket.entity.enums.ExpertAvailability;
import com.planb.supportticket.entity.enums.ExpertSpecialization;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "experts",
       indexes = {
           @Index(name = "idx_expert_availability", columnList = "availability"),
           @Index(name = "idx_expert_rating", columnList = "average_rating")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expert extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ElementCollection(targetClass = ExpertSpecialization.class)
    @CollectionTable(
        name = "expert_specializations",
        joinColumns = @JoinColumn(name = "expert_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "specialization")
    private Set<ExpertSpecialization> specializations = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "availability", nullable = false)
    private ExpertAvailability availability;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "total_ratings")
    private Integer totalRatings;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @Column(name = "available_to")
    private LocalDateTime availableTo;

    @OneToMany(mappedBy = "assignedExpert")
    private List<Ticket> assignedTickets = new ArrayList<>();

    @OneToMany(mappedBy = "expert")
    private List<Consultation> consultations = new ArrayList<>();

    /**
     * Gets the expert's rating.
     *
     * @return the expert's rating
     */
    public Double getRating() {
        return averageRating;
    }

    /**
     * Checks if the expert is available.
     *
     * @return true if the expert is available, false otherwise
     */
    public boolean isAvailable() {
        return availability == ExpertAvailability.AVAILABLE;
    }

    @OneToMany(mappedBy = "expert", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpertAvailabilitySchedule> availabilitySchedule = new ArrayList<>();

    @OneToMany(mappedBy = "expert", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatSession> chatSessions = new ArrayList<>();

    /**
     * Adds a specialization to the expert.
     *
     * @param specialization the specialization to add
     */
    public void addSpecialization(ExpertSpecialization specialization) {
        specializations.add(specialization);
    }

    /**
     * Removes a specialization from the expert.
     *
     * @param specialization the specialization to remove
     */
    public void removeSpecialization(ExpertSpecialization specialization) {
        specializations.remove(specialization);
    }

    /**
     * Adds an availability schedule to the expert.
     *
     * @param schedule the availability schedule to add
     */
    public void addAvailabilitySchedule(ExpertAvailabilitySchedule schedule) {
        availabilitySchedule.add(schedule);
        schedule.setExpert(this);
    }

    /**
     * Removes an availability schedule from the expert.
     *
     * @param schedule the availability schedule to remove
     */
    public void removeAvailabilitySchedule(ExpertAvailabilitySchedule schedule) {
        availabilitySchedule.remove(schedule);
        schedule.setExpert(null);
    }
}
