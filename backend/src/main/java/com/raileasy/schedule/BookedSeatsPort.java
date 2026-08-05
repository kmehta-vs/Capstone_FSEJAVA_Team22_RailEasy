package com.raileasy.schedule;

import com.raileasy.common.TravelClass;

import java.util.List;
import java.util.UUID;

/**
 * Port that supplies confirmed-booking seat information for a schedule + class.
 *
 * <p>In Sprint 1 there is no Booking module yet, so {@link NoBookingsPort} provides
 * an empty default. In Sprint 3 the booking module supplies a real implementation
 * (marked {@code @Primary}) backed by the bookings table.
 */
public interface BookedSeatsPort {

    /**
     * @return the seat labels (e.g. "1A", "3C") currently booked (CONFIRMED) for the
     * given schedule and travel class.
     */
    List<String> bookedSeats(UUID scheduleId, TravelClass travelClass);
}
