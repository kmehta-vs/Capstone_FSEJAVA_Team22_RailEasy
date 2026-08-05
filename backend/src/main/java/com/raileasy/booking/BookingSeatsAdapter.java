package com.raileasy.booking;

import com.raileasy.common.TravelClass;
import com.raileasy.schedule.BookedSeatsPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Sprint 3 implementation of {@link BookedSeatsPort} backed by the bookings table.
 * Marked {@code @Primary} so it overrides {@code NoBookingsPort}.
 *
 * <p>Booked seats = union of seat labels from all CONFIRMED bookings for the
 * schedule + travel class.
 */
@Primary
@Component
public class BookingSeatsAdapter implements BookedSeatsPort {

    private final BookingRepository bookingRepository;

    public BookingSeatsAdapter(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<String> bookedSeats(UUID scheduleId, TravelClass travelClass) {
        return bookingRepository
                .findByScheduleIdAndTravelClassAndStatus(scheduleId, travelClass, BookingStatus.CONFIRMED)
                .stream()
                .flatMap(b -> b.seatList().stream())
                .distinct()
                .toList();
    }
}
