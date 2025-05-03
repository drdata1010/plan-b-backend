package com.planb.supportticket.service;

import com.planb.supportticket.dto.ExpertDTO;
import com.planb.supportticket.dto.ExpertAvailabilityDTO;
import com.planb.supportticket.dto.ExpertSessionDTO;
import com.planb.supportticket.entity.ExpertSession;
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
     * Schedules a session with an expert.
     *
     * @param expertId the expert ID
     * @param sessionDTO the session data
     * @param userId the ID of the user scheduling the session
     * @return the scheduled session
     */
    ExpertSession scheduleExpertSession(UUID expertId, ExpertSessionDTO sessionDTO, UUID userId);

    /**
     * Gets a session by ID.
     *
     * @param sessionId the session ID
     * @return the session
     */
    ExpertSession getExpertSessionById(UUID sessionId);

    /**
     * Updates a session.
     *
     * @param sessionId the session ID
     * @param sessionDTO the updated session data
     * @return the updated session
     */
    ExpertSession updateExpertSession(UUID sessionId, ExpertSessionDTO sessionDTO);

    /**
     * Cancels a session.
     *
     * @param sessionId the session ID
     * @param reason the cancellation reason
     * @param cancelledBy the ID of the user cancelling the session
     * @return the cancelled session
     */
    ExpertSession cancelExpertSession(UUID sessionId, String reason, UUID cancelledBy);

    /**
     * Gets sessions for an expert with pagination.
     *
     * @param expertId the expert ID
     * @param pageable the pagination information
     * @return a page of sessions
     */
    Page<ExpertSession> getExpertSessionsByExpertId(UUID expertId, Pageable pageable);

    /**
     * Gets sessions for a user with pagination.
     *
     * @param userId the user ID
     * @param pageable the pagination information
     * @return a page of sessions
     */
    Page<ExpertSession> getExpertSessionsByUserId(UUID userId, Pageable pageable);

    /**
     * Gets upcoming sessions for an expert.
     *
     * @param expertId the expert ID
     * @return a list of upcoming sessions
     */
    List<ExpertSession> getUpcomingSessionsForExpert(UUID expertId);

    /**
     * Gets upcoming sessions for a user.
     *
     * @param userId the user ID
     * @return a list of upcoming sessions
     */
    List<ExpertSession> getUpcomingSessionsForUser(UUID userId);

    /**
     * Completes a session.
     *
     * @param sessionId the session ID
     * @param notes the completion notes
     * @return the completed session
     */
    ExpertSession completeExpertSession(UUID sessionId, String notes);

    /**
     * Rates a session.
     *
     * @param sessionId the session ID
     * @param rating the rating (1-5)
     * @param feedback the feedback
     * @param userId the ID of the user providing the rating
     * @return the rated session
     */
    ExpertSession rateExpertSession(UUID sessionId, int rating, String feedback, UUID userId);

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
     * Adds a technology to an expert.
     *
     * @param expertId the expert ID
     * @param technology the technology to add
     * @return the updated expert profile
     */
    Expert addTechnology(UUID expertId, String technology);

    /**
     * Removes a technology from an expert.
     *
     * @param expertId the expert ID
     * @param technology the technology to remove
     * @return the updated expert profile
     */
    Expert removeTechnology(UUID expertId, String technology);

    /**
     * Gets technologies for an expert.
     *
     * @param expertId the expert ID
     * @return a set of technologies
     */
    Set<String> getTechnologies(UUID expertId);

    /**
     * Adds a module to an expert.
     *
     * @param expertId the expert ID
     * @param module the module to add
     * @return the updated expert profile
     */
    Expert addModule(UUID expertId, String module);

    /**
     * Removes a module from an expert.
     *
     * @param expertId the expert ID
     * @param module the module to remove
     * @return the updated expert profile
     */
    Expert removeModule(UUID expertId, String module);

    /**
     * Gets modules for an expert.
     *
     * @param expertId the expert ID
     * @return a set of modules
     */
    Set<String> getModules(UUID expertId);

    /**
     * Searches for experts by keyword.
     *
     * @param keyword the search keyword
     * @param pageable the pagination information
     * @return a page of expert profiles
     */
    Page<Expert> searchExperts(String keyword, Pageable pageable);

    /**
     * Updates an expert entity.
     *
     * @param expert the expert entity to update
     * @return the updated expert entity
     */
    Expert updateExpert(Expert expert);
}
