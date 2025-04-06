package com.planb.supportticket.controller;

import com.planb.supportticket.dto.ExpertAvailabilityDTO;
import com.planb.supportticket.dto.ExpertDTO;
import com.planb.supportticket.dto.ExpertResponse;
import com.planb.supportticket.entity.Expert;
import com.planb.supportticket.entity.ExpertAvailabilitySchedule;
import com.planb.supportticket.service.ExpertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for expert operations.
 * Handles expert profile management, availability, and specializations.
 */
@RestController
@RequestMapping("/api/experts")
@RequiredArgsConstructor
@Slf4j
public class ExpertController {
    private final ExpertService expertService;

    /**
     * Creates a new expert profile.
     *
     * @param expertDTO the expert data
     * @param userDetails the authenticated user
     * @return the created expert profile
     */
    @PostMapping
    public ResponseEntity<ExpertResponse> createExpertProfile(
            @Valid @RequestBody ExpertDTO expertDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        Expert expert = expertService.createExpertProfile(expertDTO, userId);

        ExpertResponse response = convertToResponse(expert);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets an expert profile by ID.
     *
     * @param id the expert ID
     * @return the expert profile
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExpertResponse> getExpertById(@PathVariable UUID id) {
        Expert expert = expertService.getExpertById(id);

        ExpertResponse response = convertToResponse(expert);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets an expert profile by user ID.
     *
     * @param userId the user ID
     * @return the expert profile
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ExpertResponse> getExpertByUserId(@PathVariable UUID userId) {
        Expert expert = expertService.getExpertByUserId(userId);

        ExpertResponse response = convertToResponse(expert);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an expert profile.
     *
     * @param id the expert ID
     * @param expertDTO the updated expert data
     * @return the updated expert profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExpertResponse> updateExpertProfile(
            @PathVariable UUID id,
            @Valid @RequestBody ExpertDTO expertDTO) {

        Expert expert = expertService.updateExpertProfile(id, expertDTO);

        ExpertResponse response = convertToResponse(expert);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets all expert profiles with pagination.
     *
     * @param pageable the pagination information
     * @return a page of expert profiles
     */
    @GetMapping
    public ResponseEntity<Page<ExpertResponse>> getAllExperts(Pageable pageable) {
        Page<Expert> experts = expertService.getAllExperts(pageable);

        Page<ExpertResponse> response = experts.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets available experts with pagination.
     *
     * @param pageable the pagination information
     * @return a page of available expert profiles
     */
    @GetMapping("/available")
    public ResponseEntity<Page<ExpertResponse>> getAvailableExperts(Pageable pageable) {
        Page<Expert> experts = expertService.getAvailableExperts(pageable);

        Page<ExpertResponse> response = experts.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets experts by specialization with pagination.
     *
     * @param specialization the specialization
     * @param pageable the pagination information
     * @return a page of expert profiles
     */
    @GetMapping("/specialization")
    public ResponseEntity<Page<ExpertResponse>> getExpertsBySpecialization(
            @RequestParam String specialization,
            Pageable pageable) {

        Page<Expert> experts = expertService.getExpertsBySpecialization(specialization, pageable);

        Page<ExpertResponse> response = experts.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Sets an expert's availability status.
     *
     * @param id the expert ID
     * @param isAvailable the availability status
     * @return the updated expert profile
     */
    @PatchMapping("/{id}/availability-status")
    public ResponseEntity<ExpertResponse> setAvailabilityStatus(
            @PathVariable UUID id,
            @RequestParam boolean isAvailable) {

        Expert expert = expertService.setAvailabilityStatus(id, isAvailable);

        ExpertResponse response = convertToResponse(expert);
        return ResponseEntity.ok(response);
    }

    /**
     * Sets an expert's availability time range.
     *
     * @param id the expert ID
     * @param availableFrom the start time
     * @param availableTo the end time
     * @return the updated expert profile
     */
    @PatchMapping("/{id}/availability-time")
    public ResponseEntity<ExpertResponse> setAvailabilityTimeRange(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime availableFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime availableTo) {

        Expert expert = expertService.setAvailabilityTimeRange(id, availableFrom, availableTo);

        ExpertResponse response = convertToResponse(expert);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds an availability schedule for an expert.
     *
     * @param id the expert ID
     * @param availabilityDTO the availability data
     * @return the created availability schedule
     */
    @PostMapping("/{id}/schedule")
    public ResponseEntity<ExpertAvailabilityDTO> addAvailabilitySchedule(
            @PathVariable UUID id,
            @Valid @RequestBody ExpertAvailabilityDTO availabilityDTO) {

        ExpertAvailabilitySchedule schedule = expertService.addAvailabilitySchedule(id, availabilityDTO);

        ExpertAvailabilityDTO response = convertToAvailabilityDTO(schedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an availability schedule.
     *
     * @param id the expert ID
     * @param scheduleId the schedule ID
     * @param availabilityDTO the updated availability data
     * @return the updated availability schedule
     */
    @PutMapping("/{id}/schedule/{scheduleId}")
    public ResponseEntity<ExpertAvailabilityDTO> updateAvailabilitySchedule(
            @PathVariable UUID id,
            @PathVariable UUID scheduleId,
            @Valid @RequestBody ExpertAvailabilityDTO availabilityDTO) {

        ExpertAvailabilitySchedule schedule = expertService.updateAvailabilitySchedule(scheduleId, availabilityDTO);

        ExpertAvailabilityDTO response = convertToAvailabilityDTO(schedule);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an availability schedule.
     *
     * @param id the expert ID
     * @param scheduleId the schedule ID
     * @return no content
     */
    @DeleteMapping("/{id}/schedule/{scheduleId}")
    public ResponseEntity<Void> deleteAvailabilitySchedule(
            @PathVariable UUID id,
            @PathVariable UUID scheduleId) {

        expertService.deleteAvailabilitySchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets availability schedules for an expert.
     *
     * @param id the expert ID
     * @return a list of availability schedules
     */
    @GetMapping("/{id}/schedule")
    public ResponseEntity<List<ExpertAvailabilityDTO>> getAvailabilitySchedules(@PathVariable UUID id) {
        List<ExpertAvailabilitySchedule> schedules = expertService.getAvailabilitySchedules(id);

        List<ExpertAvailabilityDTO> response = schedules.stream()
                .map(this::convertToAvailabilityDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Gets availability schedules for an expert by day of week.
     *
     * @param id the expert ID
     * @param dayOfWeek the day of week
     * @return a list of availability schedules
     */
    @GetMapping("/{id}/schedule/day")
    public ResponseEntity<List<ExpertAvailabilityDTO>> getAvailabilitySchedulesByDay(
            @PathVariable UUID id,
            @RequestParam DayOfWeek dayOfWeek) {

        List<ExpertAvailabilitySchedule> schedules = expertService.getAvailabilitySchedulesByDay(id, dayOfWeek);

        List<ExpertAvailabilityDTO> response = schedules.stream()
                .map(this::convertToAvailabilityDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Updates an expert's hourly rate.
     *
     * @param id the expert ID
     * @param hourlyRate the new hourly rate
     * @return the updated expert profile
     */
    @PatchMapping("/{id}/hourly-rate")
    public ResponseEntity<ExpertResponse> updateHourlyRate(
            @PathVariable UUID id,
            @RequestParam double hourlyRate) {

        Expert expert = expertService.updateHourlyRate(id, hourlyRate);

        ExpertResponse response = convertToResponse(expert);
        return ResponseEntity.ok(response);
    }

    /**
     * Adds a specialization to an expert.
     *
     * @param id the expert ID
     * @param specialization the specialization to add
     * @return the updated expert profile
     */
    @PostMapping("/{id}/specializations")
    public ResponseEntity<ExpertResponse> addSpecialization(
            @PathVariable UUID id,
            @RequestParam String specialization) {

        Expert expert = expertService.addSpecialization(id, specialization);

        ExpertResponse response = convertToResponse(expert);
        return ResponseEntity.ok(response);
    }

    /**
     * Removes a specialization from an expert.
     *
     * @param id the expert ID
     * @param specialization the specialization to remove
     * @return the updated expert profile
     */
    @DeleteMapping("/{id}/specializations")
    public ResponseEntity<ExpertResponse> removeSpecialization(
            @PathVariable UUID id,
            @RequestParam String specialization) {

        Expert expert = expertService.removeSpecialization(id, specialization);

        ExpertResponse response = convertToResponse(expert);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets specializations for an expert.
     *
     * @param id the expert ID
     * @return a set of specializations
     */
    @GetMapping("/{id}/specializations")
    public ResponseEntity<Set<String>> getSpecializations(@PathVariable UUID id) {
        Set<String> specializations = expertService.getSpecializations(id);
        return ResponseEntity.ok(specializations);
    }

    /**
     * Gets the average rating for an expert.
     *
     * @param id the expert ID
     * @return the average rating
     */
    @GetMapping("/{id}/rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable UUID id) {
        double rating = expertService.getAverageRating(id);
        return ResponseEntity.ok(rating);
    }

    /**
     * Searches for experts by keyword.
     *
     * @param keyword the search keyword
     * @param pageable the pagination information
     * @return a page of expert profiles
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ExpertResponse>> searchExperts(
            @RequestParam String keyword,
            Pageable pageable) {

        Page<Expert> experts = expertService.searchExperts(keyword, pageable);

        Page<ExpertResponse> response = experts.map(this::convertToResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Converts an Expert entity to an ExpertResponse DTO.
     *
     * @param expert the expert entity
     * @return the expert response DTO
     */
    private ExpertResponse convertToResponse(Expert expert) {
        ExpertResponse response = new ExpertResponse();
        response.setId(expert.getId());
        response.setBio(expert.getBio());
        response.setSpecializations(expert.getSpecializations().stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toSet()));
        response.setHourlyRate(expert.getHourlyRate() != null ?
                new java.math.BigDecimal(expert.getHourlyRate().toString()) : null);
        response.setRating(expert.getRating());
        response.setAvailable(expert.isAvailable());
        response.setAvailableFrom(expert.getAvailableFrom());
        response.setAvailableTo(expert.getAvailableTo());

        if (expert.getUserProfile() != null) {
            response.setUserId(expert.getUserProfile().getId());
            response.setDisplayName(expert.getUserProfile().getDisplayName());
            response.setEmail(expert.getUserProfile().getEmail());
            response.setProfilePictureUrl(expert.getUserProfile().getProfilePictureUrl());
        }

        return response;
    }

    /**
     * Converts an ExpertAvailabilitySchedule entity to an ExpertAvailabilityDTO.
     *
     * @param schedule the expert availability schedule entity
     * @return the expert availability DTO
     */
    private ExpertAvailabilityDTO convertToAvailabilityDTO(ExpertAvailabilitySchedule schedule) {
        ExpertAvailabilityDTO dto = new ExpertAvailabilityDTO();
        dto.setId(schedule.getId());
        dto.setExpertId(schedule.getExpert().getId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setAvailable(schedule.isAvailable());
        dto.setMaxConsultations(schedule.getMaxConsultations());
        dto.setNotes(schedule.getNotes());
        return dto;
    }

    /**
     * Gets the user ID from the UserDetails.
     *
     * @param userDetails the user details
     * @return the user ID
     */
    private UUID getUserIdFromUserDetails(UserDetails userDetails) {
        // In a real implementation, you would extract the user ID from the UserDetails
        // This is a placeholder implementation
        return UUID.fromString(userDetails.getUsername());
    }
}
