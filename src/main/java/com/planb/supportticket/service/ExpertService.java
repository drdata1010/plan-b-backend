package com.planb.supportticket.service;

import com.planb.supportticket.dto.ConsultationDTO;
import com.planb.supportticket.dto.ExpertDTO;
import com.planb.supportticket.dto.ExpertAvailabilityDTO;
import com.planb.supportticket.entity.Consultation;
import com.planb.supportticket.entity.Expert;
import com.planb.supportticket.entity.ExpertAvailabilitySchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service interface for expert management operations.
 * Handles profile management, availability handling, consultation scheduling, and rating system.
 */
public interface ExpertService {

    /**
     * Creates a new expert profile.
     *
     * @param expertDTO the expert data
     * @param userId the ID of the user to associate with the expert profile
     * @return the created expert profile
     */
    Expert createExpertProfile(ExpertDTO expertDTO, UUID userId);

    /**
     * Gets an expert profile by ID.
     *
     * @param id the expert ID
     * @return the expert profile
     */
    Expert getExpertById(UUID id);

    /**
     * Gets an expert profile by user ID.
     *
     * @param userId the user ID
     * @return the expert profile
     */
    Expert getExpertByUserId(UUID userId);

    /**
     * Updates an expert profile.
     *
     * @param id the expert ID
     * @param expertDTO the updated expert data
     * @return the updated expert profile
     */
    Expert updateExpertProfile(UUID id, ExpertDTO expertDTO);

    /**
     * Deletes an expert profile.
     *
     * @param id the expert ID
     */
    void deleteExpertProfile(UUID id);

    /**
     * Gets all expert profiles with pagination.
     *
     * @param pageable the pagination information
     * @return a page of expert profiles
     */
    Page<Expert> getAllExperts(Pageable pageable);

    /**
     * Gets available experts with pagination.
     *
     * @param pageable the pagination information
     * @return a page of available expert profiles
     */
    Page<Expert> getAvailableExperts(Pageable pageable);

    /**
     * Gets experts by specialization with pagination.
     *
     * @param specialization the specialization
     * @param pageable the pagination information
     * @return a page of expert profiles
     */
    Page<Expert> getExpertsBySpecialization(String specialization, Pageable pageable);

    /**
     * Sets an expert's availability status.
     *
     * @param id the expert ID
     * @param isAvailable the availability status
     * @return the updated expert profile
     */
    Expert setAvailabilityStatus(UUID id, boolean isAvailable);

    /**
     * Sets an expert's availability time range.
     *
     * @param id the expert ID
     * @param availableFrom the start time
     * @param availableTo the end time
     * @return the updated expert profile
     */
    Expert setAvailabilityTimeRange(UUID id, LocalDateTime availableFrom, LocalDateTime availableTo);

    /**
     * Adds an availability schedule for an expert.
     *
     * @param expertId the expert ID
     * @param availabilityDTO the availability data
     * @return the created availability schedule
     */
    ExpertAvailabilitySchedule addAvailabilitySchedule(UUID expertId, ExpertAvailabilityDTO availabilityDTO);

    /**
     * Updates an availability schedule.
     *
     * @param scheduleId the schedule ID
     * @param availabilityDTO the updated availability data
     * @return the updated availability schedule
     */
    ExpertAvailabilitySchedule updateAvailabilitySchedule(UUID scheduleId, ExpertAvailabilityDTO availabilityDTO);

    /**
     * Deletes an availability schedule.
     *
     * @param scheduleId the schedule ID
     */
    void deleteAvailabilitySchedule(UUID scheduleId);

    /**
     * Gets availability schedules for an expert.
     *
     * @param expertId the expert ID
     * @return a list of availability schedules
     */
    List<ExpertAvailabilitySchedule> getAvailabilitySchedules(UUID expertId);

    /**
     * Gets availability schedules for an expert by day of week.
     *
     * @param expertId the expert ID
     * @param dayOfWeek the day of week
     * @return a list of availability schedules
     */
    List<ExpertAvailabilitySchedule> getAvailabilitySchedulesByDay(UUID expertId, DayOfWeek dayOfWeek);

    /**
     * Schedules a consultation with an expert.
     *
     * @param expertId the expert ID
     * @param consultationDTO the consultation data
     * @param userId the ID of the user scheduling the consultation
     * @return the scheduled consultation
     */
    Consultation scheduleConsultation(UUID expertId, ConsultationDTO consultationDTO, UUID userId);

    /**
     * Gets a consultation by ID.
     *
     * @param consultationId the consultation ID
     * @return the consultation
     */
    Consultation getConsultationById(UUID consultationId);

    /**
     * Updates a consultation.
     *
     * @param consultationId the consultation ID
     * @param consultationDTO the updated consultation data
     * @return the updated consultation
     */
    Consultation updateConsultation(UUID consultationId, ConsultationDTO consultationDTO);

    /**
     * Cancels a consultation.
     *
     * @param consultationId the consultation ID
     * @param reason the cancellation reason
     * @param cancelledBy the ID of the user cancelling the consultation
     * @return the cancelled consultation
     */
    Consultation cancelConsultation(UUID consultationId, String reason, UUID cancelledBy);

    /**
     * Gets consultations for an expert with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of consultations
     */
    Page<Consultation> getConsultationsByExpertId(UUID expertId, Pageable pageable);

    /**
     * Gets consultations for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of consultations
     */
    Page<Consultation> getConsultationsByUserId(UUID userId, Pageable pageable);

    /**
     * Gets upcoming consultations for an expert.
     *
     * @param expertId the expert ID
     * @return a list of upcoming consultations
     */
    List<Consultation> getUpcomingConsultationsForExpert(UUID expertId);

    /**
     * Gets upcoming consultations for a user.
     *
     * @param userId the user ID
     * @return a list of upcoming consultations
     */
    List<Consultation> getUpcomingConsultationsForUser(UUID userId);

    /**
     * Completes a consultation.
     *
     * @param consultationId the consultation ID
     * @param notes the completion notes
     * @return the completed consultation
     */
    Consultation completeConsultation(UUID consultationId, String notes);

    /**
     * Rates a consultation.
     *
     * @param consultationId the consultation ID
     * @param rating the rating (1-5)
     * @param feedback the feedback
     * @param userId the ID of the user providing the rating
     * @return the rated consultation
     */
    Consultation rateConsultation(UUID consultationId, int rating, String feedback, UUID userId);

    /**
     * Gets the average rating for an expert.
     *
     * @param expertId the expert ID
     * @return the average rating
     */
    double getAverageRating(UUID expertId);

    /**
     * Updates an expert's hourly rate.
     *
     * @param expertId the expert ID
     * @param hourlyRate the new hourly rate
     * @return the updated expert profile
     */
    Expert updateHourlyRate(UUID expertId, double hourlyRate);

    /**
     * Adds a specialization to an expert.
     *
     * @param expertId the expert ID
     * @param specialization the specialization to add
     * @return the updated expert profile
     */
    Expert addSpecialization(UUID expertId, String specialization);

    /**
     * Removes a specialization from an expert.
     *
     * @param expertId the expert ID
     * @param specialization the specialization to remove
     * @return the updated expert profile
     */
    Expert removeSpecialization(UUID expertId, String specialization);

    /**
     * Gets specializations for an expert.
     *
     * @param expertId the expert ID
     * @return a set of specializations
     */
    Set<String> getSpecializations(UUID expertId);

    /**
     * Searches for experts by keyword.
     *
     * @param keyword the search keyword
     * @param pageable the pagination information
     * @return a page of expert profiles
     */
    Page<Expert> searchExperts(String keyword, Pageable pageable);
}
