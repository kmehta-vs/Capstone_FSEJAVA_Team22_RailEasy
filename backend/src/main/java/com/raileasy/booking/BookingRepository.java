package com.raileasy.booking;

import com.raileasy.common.TravelClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /** All bookings for a user, newest first. */
    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** Bookings for a schedule + class filtered by status (used for seat availability). */
    List<Booking> findByScheduleIdAndTravelClassAndStatus(
            UUID scheduleId, TravelClass travelClass, BookingStatus status);
}
