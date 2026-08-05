package com.raileasy.schedule;

import com.raileasy.common.TravelClass;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Sprint 1 default: no bookings exist yet, so nothing is booked.
 * Replaced by the booking module's implementation (@Primary) in Sprint 3.
 */
@Component
public class NoBookingsPort implements BookedSeatsPort {

    @Override
    public List<String> bookedSeats(UUID scheduleId, TravelClass travelClass) {
        return List.of();
    }
}
