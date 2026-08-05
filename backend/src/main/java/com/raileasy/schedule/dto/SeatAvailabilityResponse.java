package com.raileasy.schedule.dto;

import com.raileasy.common.TravelClass;

import java.util.List;
import java.util.UUID;

/**
 * Booked seat numbers for a schedule + travel class (used by the seat grid).
 */
public record SeatAvailabilityResponse(
        UUID scheduleId,
        TravelClass travelClass,
        int seatsPerClass,
        List<String> bookedSeats
) {
}
