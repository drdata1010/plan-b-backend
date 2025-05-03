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
import java.util.stream.Collectors;

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

    @ElementCollection
    @CollectionTable(
        name = "expert_technologies",
        joinColumns = @JoinColumn(name = "expert_id")
    )
    @Column(name = "technology")
    private Set<String> technologies = new HashSet<>();

    // For backward compatibility
    public Set<ExpertSpecialization> getSpecializations() {
        return technologies.stream()
                .map(tech -> {
                    try {
                        return ExpertSpecialization.valueOf(tech);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(spec -> spec != null)
                .collect(Collectors.toSet());
    }

    public void setSpecializations(Set<ExpertSpecialization> specializations) {
        this.technologies = specializations.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @ElementCollection
    @CollectionTable(
        name = "expert_modules",
        joinColumns = @JoinColumn(name = "expert_id")
    )
    @Column(name = "module")
    private Set<String> modules = new HashSet<>();

    @Column(name = "other_expertise", length = 500)
    private String otherExpertise;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability", nullable = false)
    private ExpertAvailability availability;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "years_of_experience")
    private String yearsOfExperience; // 0-1, 2-4, 5-8, 9-12, 12+

    @Column(name = "job_role")
    private String jobRole;

    @Column(name = "response_time")
    private String responseTime; // Within 2 hours, Within 5 hours, Within 8 hours

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "total_ratings")
    private Integer totalRatings;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    @Column(name = "linkedin_profile")
    private String linkedinProfile;

    @Column(name = "available_from")
    private LocalDateTime availableFrom;

    @Column(name = "available_to")
    private LocalDateTime availableTo;

    @OneToMany(mappedBy = "assignedExpert")
    private List<Ticket> assignedTickets = new ArrayList<>();

    @OneToMany(mappedBy = "expert")
    private List<ExpertSession> expertSessions = new ArrayList<>();

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
     * Adds a technology to the expert.
     *
     * @param technology the technology to add
     */
    public void addTechnology(String technology) {
        technologies.add(technology);
    }

    /**
     * Removes a technology from the expert.
     *
     * @param technology the technology to remove
     */
    public void removeTechnology(String technology) {
        technologies.remove(technology);
    }

    /**
     * Adds a module to the expert.
     *
     * @param module the module to add
     */
    public void addModule(String module) {
        modules.add(module);
    }

    /**
     * Removes a module from the expert.
     *
     * @param module the module to remove
     */
    public void removeModule(String module) {
        modules.remove(module);
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
