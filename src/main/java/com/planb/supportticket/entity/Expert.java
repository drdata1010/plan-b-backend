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
}
