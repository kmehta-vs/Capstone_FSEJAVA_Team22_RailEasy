package com.raileasy.schedule.dto;

import com.raileasy.schedule.Schedule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Full schedule representation for admin management screens.
 */
public record ScheduleResponse(
        UUID id,
        UUID trainId,
        String trainName,
        String trainNumber,
        String fromStation,
        String toStation,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        LocalDate journeyDate,
        BigDecimal fareSleeper,
        BigDecimal fareAc3,
        BigDecimal fareAc2
) {
    public static ScheduleResponse from(Schedule s) {
        return new ScheduleResponse(
                s.getId(),
                s.getTrain().getId(),
                s.getTrain().getTrainName(),
                s.getTrain().getTrainNumber(),
                s.getFromStation(),
                s.getToStation(),
                s.getDepartureTime(),
                s.getArrivalTime(),
                s.getJourneyDate(),
                s.getFareSleeper(),
                s.getFareAc3(),
                s.getFareAc2()
        );
    }
}
