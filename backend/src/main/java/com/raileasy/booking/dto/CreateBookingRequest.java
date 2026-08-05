package com.raileasy.booking.dto;

import com.raileasy.common.TravelClass;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Request to create a booking. 1–4 seats per booking (per business rules).
 */
public record CreateBookingRequest(
        @NotNull UUID scheduleId,
        @NotNull TravelClass travelClass,
        @NotEmpty(message = "Select at least one seat")
        @Size(min = 1, max = 4, message = "You can book between 1 and 4 seats")
        List<String> seatNumbers
) {
}
