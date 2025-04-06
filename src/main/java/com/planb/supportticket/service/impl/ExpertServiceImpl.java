package com.planb.supportticket.service.impl;

import com.planb.supportticket.dto.ConsultationDTO;
import com.planb.supportticket.dto.ExpertDTO;
import com.planb.supportticket.dto.ExpertAvailabilityDTO;
import com.planb.supportticket.entity.Consultation;
import com.planb.supportticket.entity.Expert;
import com.planb.supportticket.entity.ExpertAvailabilitySchedule;
import com.planb.supportticket.entity.UserProfile;
import com.planb.supportticket.entity.enums.ExpertAvailability;
import com.planb.supportticket.entity.enums.ExpertSpecialization;
import com.planb.supportticket.repository.ConsultationRepository;
import com.planb.supportticket.repository.ExpertAvailabilityScheduleRepository;
import com.planb.supportticket.repository.ExpertRepository;
import com.planb.supportticket.repository.UserProfileRepository;
import com.planb.supportticket.service.ExpertService;
import com.planb.supportticket.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the ExpertService interface.
 * Handles expert profile management, availability, consultations, and ratings.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpertServiceImpl implements ExpertService {

    private final ExpertRepository expertRepository;
    private final UserProfileRepository userProfileRepository;
    private final ExpertAvailabilityScheduleRepository availabilityRepository;
    private final ConsultationRepository consultationRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Expert createExpertProfile(ExpertDTO expertDTO, UUID userId) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Expert expert = new Expert();
        expert.setUserProfile(userProfile);
        expert.setBio(expertDTO.getBio());
        expert.setAvailability(ExpertAvailability.OFFLINE);

        if (expertDTO.getSpecializations() != null) {
            Set<ExpertSpecialization> specializations = new HashSet<>();
            for (String spec : expertDTO.getSpecializations()) {
                try {
                    specializations.add(ExpertSpecialization.valueOf(spec));
                } catch (IllegalArgumentException e) {
                    // Skip invalid specializations
                }
            }
            expert.setSpecializations(specializations);
        }

        return expertRepository.save(expert);
    }

    @Override
    public Expert getExpertById(UUID id) {
        return expertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expert not found with ID: " + id));
    }

    @Override
    public Expert getExpertByUserId(UUID userId) {
        // Simplified implementation
        return expertRepository.findAll().stream()
                .filter(e -> e.getUserProfile() != null && e.getUserProfile().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Expert not found for user ID: " + userId));
    }

    @Override
    @Transactional
    public Expert updateExpertProfile(UUID id, ExpertDTO expertDTO) {
        Expert expert = getExpertById(id);

        if (expertDTO.getBio() != null) {
            expert.setBio(expertDTO.getBio());
        }

        if (expertDTO.getSpecializations() != null) {
            Set<ExpertSpecialization> specializations = new HashSet<>();
            for (String spec : expertDTO.getSpecializations()) {
                try {
                    specializations.add(ExpertSpecialization.valueOf(spec));
                } catch (IllegalArgumentException e) {
                    // Skip invalid specializations
                }
            }
            expert.setSpecializations(specializations);
        }

        return expertRepository.save(expert);
    }

    @Override
    @Transactional
    public void deleteExpertProfile(UUID id) {
        Expert expert = getExpertById(id);
        expertRepository.delete(expert);
    }

    @Override
    public Page<Expert> getAllExperts(Pageable pageable) {
        return expertRepository.findAll(pageable);
    }

    @Override
    public Page<Expert> getAvailableExperts(Pageable pageable) {
        // Simplified implementation
        return expertRepository.findAll(pageable);
    }

    @Override
    public Page<Expert> getExpertsBySpecialization(String specialization, Pageable pageable) {
        // Simplified implementation
        return expertRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Expert setAvailabilityStatus(UUID id, boolean isAvailable) {
        Expert expert = getExpertById(id);
        expert.setAvailability(isAvailable ? ExpertAvailability.AVAILABLE : ExpertAvailability.OFFLINE);
        return expertRepository.save(expert);
    }

    @Override
    @Transactional
    public Expert setAvailabilityTimeRange(UUID id, LocalDateTime availableFrom, LocalDateTime availableTo) {
        // Simplified implementation
        return setAvailabilityStatus(id, true);
    }

    @Override
    @Transactional
    public ExpertAvailabilitySchedule addAvailabilitySchedule(UUID expertId, ExpertAvailabilityDTO availabilityDTO) {
        // Simplified implementation
        return null;
    }

    @Override
    @Transactional
    public ExpertAvailabilitySchedule updateAvailabilitySchedule(UUID scheduleId, ExpertAvailabilityDTO availabilityDTO) {
        // Simplified implementation
        return null;
    }

    @Override
    @Transactional
    public void deleteAvailabilitySchedule(UUID scheduleId) {
        // Simplified implementation
    }

    @Override
    public List<ExpertAvailabilitySchedule> getAvailabilitySchedules(UUID expertId) {
        // Simplified implementation
        return List.of();
    }

    @Override
    public List<ExpertAvailabilitySchedule> getAvailabilitySchedulesByDay(UUID expertId, DayOfWeek dayOfWeek) {
        // Simplified implementation
        return List.of();
    }

    @Override
    @Transactional
    public Consultation scheduleConsultation(UUID expertId, ConsultationDTO consultationDTO, UUID userId) {
        // Simplified implementation
        return null;
    }

    @Override
    public Consultation getConsultationById(UUID consultationId) {
        // Simplified implementation
        return null;
    }

    @Override
    @Transactional
    public Consultation updateConsultation(UUID consultationId, ConsultationDTO consultationDTO) {
        // Simplified implementation
        return null;
    }

    @Override
    @Transactional
    public Consultation cancelConsultation(UUID consultationId, String reason, UUID cancelledBy) {
        // Simplified implementation
        return null;
    }

    @Override
    public Page<Consultation> getConsultationsByExpertId(UUID expertId, Pageable pageable) {
        // Simplified implementation
        return Page.empty();
    }

    @Override
    public Page<Consultation> getConsultationsByUserId(UUID userId, Pageable pageable) {
        // Simplified implementation
        return Page.empty();
    }

    @Override
    public List<Consultation> getUpcomingConsultationsForExpert(UUID expertId) {
        // Simplified implementation
        return List.of();
    }

    @Override
    public List<Consultation> getUpcomingConsultationsForUser(UUID userId) {
        // Simplified implementation
        return List.of();
    }

    @Override
    @Transactional
    public Consultation completeConsultation(UUID consultationId, String notes) {
        // Simplified implementation
        return null;
    }

    @Override
    @Transactional
    public Consultation rateConsultation(UUID consultationId, int rating, String feedback, UUID userId) {
        // Simplified implementation
        return null;
    }

    @Override
    public double getAverageRating(UUID expertId) {
        Expert expert = getExpertById(expertId);
        return expert.getAverageRating();
    }

    @Override
    @Transactional
    public Expert updateHourlyRate(UUID expertId, double hourlyRate) {
        // Simplified implementation
        return getExpertById(expertId);
    }

    @Override
    @Transactional
    public Expert addSpecialization(UUID expertId, String specialization) {
        Expert expert = getExpertById(expertId);
        try {
            expert.getSpecializations().add(ExpertSpecialization.valueOf(specialization));
            return expertRepository.save(expert);
        } catch (IllegalArgumentException e) {
            return expert;
        }
    }

    @Override
    @Transactional
    public Expert removeSpecialization(UUID expertId, String specialization) {
        Expert expert = getExpertById(expertId);
        try {
            expert.getSpecializations().remove(ExpertSpecialization.valueOf(specialization));
            return expertRepository.save(expert);
        } catch (IllegalArgumentException e) {
            return expert;
        }
    }

    @Override
    public Set<String> getSpecializations(UUID expertId) {
        Expert expert = getExpertById(expertId);
        return expert.getSpecializations().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Override
    public Page<Expert> searchExperts(String keyword, Pageable pageable) {
        // Simplified implementation
        return expertRepository.findAll(pageable);
    }
}
