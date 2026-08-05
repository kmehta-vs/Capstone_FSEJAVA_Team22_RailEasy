package com.raileasy.schedule.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A schedule search result: train info, timings and per-class availability/fares.
 */
public record ScheduleSearchResponse(
        UUID scheduleId,
        String trainName,
        String trainNumber,
        String fromStation,
        String toStation,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        LocalDate journeyDate,
        List<ClassAvailability> classes
) {
}
