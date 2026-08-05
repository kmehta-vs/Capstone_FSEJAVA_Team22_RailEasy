package com.raileasy.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Admin payload to create/update a schedule.
 */
public record ScheduleRequest(
        @NotNull UUID trainId,
        @NotBlank String fromStation,
        @NotBlank String toStation,
        @NotNull LocalDateTime departureTime,
        @NotNull LocalDateTime arrivalTime,
        @NotNull LocalDate journeyDate,
        @NotNull @Positive BigDecimal fareSleeper,
        @NotNull @Positive BigDecimal fareAc3,
        @NotNull @Positive BigDecimal fareAc2
) {
}
