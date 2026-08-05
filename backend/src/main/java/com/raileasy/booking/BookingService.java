package com.raileasy.booking;

import com.raileasy.booking.dto.BookingResponse;
import com.raileasy.booking.dto.CreateBookingRequest;
import com.raileasy.common.BadRequestException;
import com.raileasy.common.ConflictException;
import com.raileasy.common.NotFoundException;
import com.raileasy.common.PnrGenerator;
import com.raileasy.common.TravelClass;
import com.raileasy.schedule.Schedule;
import com.raileasy.schedule.ScheduleRepository;
import com.raileasy.user.User;
import com.raileasy.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Booking business logic: create (with PNR + double-book guard), list mine, cancel.
 */
@Service
public class BookingService {

    private static final int MIN_SEATS = 1;
    private static final int MAX_SEATS = 4;

    private final BookingRepository bookingRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          ScheduleRepository scheduleRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse create(String userEmail, CreateBookingRequest request) {
        User user = requireUser(userEmail);
        Schedule schedule = scheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new NotFoundException("Schedule not found: " + request.scheduleId()));

        List<String> requested = normalizeSeats(request.seatNumbers());
        if (requested.size() < MIN_SEATS || requested.size() > MAX_SEATS) {
            throw new BadRequestException("You can book between 1 and 4 seats");
        }
        validateSeatLabels(requested, schedule.getTrain().getSeatsPerClass());

        // Concurrency/double-book guard: re-check against currently booked seats.
        Set<String> alreadyBooked = new HashSet<>(currentlyBookedSeats(request.scheduleId(), request.travelClass()));
        List<String> clashes = requested.stream().filter(alreadyBooked::contains).toList();
        if (!clashes.isEmpty()) {
            throw new ConflictException("Seat(s) already booked: " + String.join(", ", clashes));
        }

        Booking booking = new Booking(
                user, schedule, request.travelClass(), requested, PnrGenerator.generate());
        Booking saved = bookingRepository.save(booking);
        return BookingResponse.from(saved, fareFor(schedule, request.travelClass()));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> mine(String userEmail) {
        User user = requireUser(userEmail);
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(b -> BookingResponse.from(b, fareFor(b.getSchedule(), b.getTravelClass())))
                .toList();
    }

    @Transactional
    public BookingResponse cancel(String userEmail, UUID bookingId) {
        User user = requireUser(userEmail);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));

        if (!java.util.Objects.equals(booking.getUser().getId(), user.getId())) {
            throw new BadRequestException("You can only cancel your own bookings");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking is already cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        return BookingResponse.from(saved, fareFor(saved.getSchedule(), saved.getTravelClass()));
    }

    private List<String> currentlyBookedSeats(UUID scheduleId, TravelClass travelClass) {
        return bookingRepository
                .findByScheduleIdAndTravelClassAndStatus(scheduleId, travelClass, BookingStatus.CONFIRMED)
                .stream()
                .flatMap(b -> b.seatList().stream())
                .toList();
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    private static List<String> normalizeSeats(List<String> seats) {
        return seats.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toUpperCase())
                .distinct()
                .toList();
    }

    private static void validateSeatLabels(List<String> seats, int seatsPerClass) {
        int rows = seatsPerClass / 8; // 8 columns A..H
        for (String seat : seats) {
            if (!seat.matches("[1-8][A-H]")) {
                throw new BadRequestException("Invalid seat label: " + seat);
            }
            int row = seat.charAt(0) - '0';
            if (row < 1 || row > rows) {
                throw new BadRequestException("Seat out of range: " + seat);
            }
        }
    }

    static BigDecimal fareFor(Schedule schedule, TravelClass travelClass) {
        return switch (travelClass) {
            case SLEEPER -> schedule.getFareSleeper();
            case AC_3 -> schedule.getFareAc3();
            case AC_2 -> schedule.getFareAc2();
        };
    }
}
