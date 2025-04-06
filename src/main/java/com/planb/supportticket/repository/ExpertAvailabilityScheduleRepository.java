package com.planb.supportticket.repository;

import com.planb.supportticket.entity.ExpertAvailabilitySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ExpertAvailabilitySchedule entities.
 */
@Repository
public interface ExpertAvailabilityScheduleRepository extends JpaRepository<ExpertAvailabilitySchedule, UUID> {

    /**
     * Finds availability schedules by expert ID.
     *
     * @param expertId the expert ID
     * @return a list of availability schedules
     */
    List<ExpertAvailabilitySchedule> findByExpertId(UUID expertId);

    /**
     * Finds availability schedules by expert ID and day of week.
     *
     * @param expertId the expert ID
     * @param dayOfWeek the day of week
     * @return a list of availability schedules
     */
    List<ExpertAvailabilitySchedule> findByExpertIdAndDayOfWeek(UUID expertId, DayOfWeek dayOfWeek);

    /**
     * Finds availability schedules by expert ID and availability.
     *
     * @param expertId the expert ID
     * @param isAvailable the availability flag
     * @return a list of availability schedules
     */
    List<ExpertAvailabilitySchedule> findByExpertIdAndIsAvailable(UUID expertId, boolean isAvailable);
}
