package com.raileasy.booking;

import com.raileasy.booking.dto.BookingResponse;
import com.raileasy.booking.dto.CreateBookingRequest;
import com.raileasy.common.BadRequestException;
import com.raileasy.common.ConflictException;
import com.raileasy.common.NotFoundException;
import com.raileasy.common.TravelClass;
import com.raileasy.schedule.Schedule;
import com.raileasy.schedule.ScheduleRepository;
import com.raileasy.train.Train;
import com.raileasy.user.User;
import com.raileasy.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final String EMAIL = "rider@raileasy.com";

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        user = new User(EMAIL, "hash", "Rider", false);
        Train train = new Train("12163", "Chennai Express", 64);
        schedule = new Schedule(
                train, "Chennai Central", "Mumbai CSMT",
                LocalDateTime.of(2025, 10, 21, 6, 0),
                LocalDateTime.of(2025, 10, 22, 5, 30),
                LocalDate.of(2025, 10, 21),
                new BigDecimal("450.00"), new BigDecimal("1200.00"), new BigDecimal("1800.00"));
        lenient().when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        lenient().when(scheduleRepository.findById(any())).thenReturn(Optional.of(schedule));
    }

    @Test
    void create_generatesPnrAndComputesTotalFare() {
        when(bookingRepository.findByScheduleIdAndTravelClassAndStatus(any(), any(), any()))
                .thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new CreateBookingRequest(schedule.getId(), TravelClass.AC_3, List.of("1A", "1B"));
        BookingResponse res = bookingService.create(EMAIL, request);

        assertThat(res.pnrNumber()).hasSize(8);
        assertThat(res.seatNumbers()).containsExactly("1A", "1B");
        assertThat(res.farePerSeat()).isEqualByComparingTo("1200.00");
        assertThat(res.totalFare()).isEqualByComparingTo("2400.00");
        assertThat(res.status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void create_conflictWhenSeatAlreadyBooked() {
        Booking existing = new Booking(user, schedule, TravelClass.AC_3, List.of("1A"), "PNR12345");
        when(bookingRepository.findByScheduleIdAndTravelClassAndStatus(any(), any(), any()))
                .thenReturn(List.of(existing));

        var request = new CreateBookingRequest(schedule.getId(), TravelClass.AC_3, List.of("1A", "1B"));

        assertThatThrownBy(() -> bookingService.create(EMAIL, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("1A");
    }

    @Test
    void create_rejectsMoreThanFourSeats() {
        var request = new CreateBookingRequest(
                schedule.getId(), TravelClass.SLEEPER, List.of("1A", "1B", "1C", "1D", "1E"));

        assertThatThrownBy(() -> bookingService.create(EMAIL, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void create_rejectsInvalidSeatLabel() {
        var request = new CreateBookingRequest(schedule.getId(), TravelClass.SLEEPER, List.of("9Z"));

        assertThatThrownBy(() -> bookingService.create(EMAIL, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void cancel_setsStatusCancelled() {
        Booking booking = new Booking(user, schedule, TravelClass.AC_2, List.of("2A"), "PNR22222");
        UUID id = UUID.randomUUID();
        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse res = bookingService.cancel(EMAIL, id);

        assertThat(res.status()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancel_conflictWhenAlreadyCancelled() {
        Booking booking = new Booking(user, schedule, TravelClass.AC_2, List.of("2A"), "PNR22222");
        booking.setStatus(BookingStatus.CANCELLED);
        UUID id = UUID.randomUUID();
        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(EMAIL, id))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void cancel_notFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(bookingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancel(EMAIL, id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void mine_returnsUserBookings() {
        Booking booking = new Booking(user, schedule, TravelClass.SLEEPER, List.of("3A"), "PNR33333");
        when(bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(booking));

        List<BookingResponse> mine = bookingService.mine(EMAIL);

        assertThat(mine).hasSize(1);
        assertThat(mine.get(0).farePerSeat()).isEqualByComparingTo("450.00");
    }
}
