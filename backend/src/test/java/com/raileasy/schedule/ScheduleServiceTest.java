package com.raileasy.schedule;

import com.raileasy.common.NotFoundException;
import com.raileasy.common.TravelClass;
import com.raileasy.schedule.dto.ClassAvailability;
import com.raileasy.schedule.dto.ScheduleSearchResponse;
import com.raileasy.schedule.dto.SeatAvailabilityResponse;
import com.raileasy.train.Train;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private com.raileasy.train.TrainRepository trainRepository;

    @Mock
    private BookedSeatsPort bookedSeatsPort;

    @InjectMocks
    private ScheduleService scheduleService;

    private Train train;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        train = new Train("12163", "Chennai Express", 64);
        schedule = new Schedule(
                train, "Chennai Central", "Mumbai CSMT",
                LocalDateTime.of(2025, 10, 21, 6, 0),
                LocalDateTime.of(2025, 10, 22, 5, 30),
                LocalDate.of(2025, 10, 21),
                new BigDecimal("450.00"), new BigDecimal("1200.00"), new BigDecimal("1800.00"));
    }

    @Test
    void search_returnsThreeClassesWithAvailabilityAndFares() {
        when(scheduleRepository.search("Chennai Central", "Mumbai CSMT", LocalDate.of(2025, 10, 21)))
                .thenReturn(List.of(schedule));
        // 2 sleeper seats booked, others empty
        when(bookedSeatsPort.bookedSeats(schedule.getId(), TravelClass.SLEEPER))
                .thenReturn(List.of("1A", "1B"));
        when(bookedSeatsPort.bookedSeats(schedule.getId(), TravelClass.AC_3)).thenReturn(List.of());
        when(bookedSeatsPort.bookedSeats(schedule.getId(), TravelClass.AC_2)).thenReturn(List.of());

        List<ScheduleSearchResponse> results =
                scheduleService.search("Chennai Central", "Mumbai CSMT", LocalDate.of(2025, 10, 21));

        assertThat(results).hasSize(1);
        ScheduleSearchResponse r = results.get(0);
        assertThat(r.trainNumber()).isEqualTo("12163");
        assertThat(r.classes()).hasSize(3);

        ClassAvailability sleeper = r.classes().stream()
                .filter(c -> c.travelClass() == TravelClass.SLEEPER).findFirst().orElseThrow();
        assertThat(sleeper.seatsAvailable()).isEqualTo(62);
        assertThat(sleeper.fare()).isEqualByComparingTo("450.00");

        ClassAvailability ac3 = r.classes().stream()
                .filter(c -> c.travelClass() == TravelClass.AC_3).findFirst().orElseThrow();
        assertThat(ac3.seatsAvailable()).isEqualTo(64);
    }

    @Test
    void search_emptyWhenNoSchedules() {
        when(scheduleRepository.search("A", "B", LocalDate.of(2025, 1, 1))).thenReturn(List.of());
        assertThat(scheduleService.search("A", "B", LocalDate.of(2025, 1, 1))).isEmpty();
    }

    @Test
    void seats_returnsBookedSeatsForClass() {
        UUID id = schedule.getId();
        when(scheduleRepository.findById(id)).thenReturn(Optional.of(schedule));
        when(bookedSeatsPort.bookedSeats(id, TravelClass.AC_3)).thenReturn(List.of("2C", "2D"));

        SeatAvailabilityResponse res = scheduleService.seats(id, TravelClass.AC_3);

        assertThat(res.seatsPerClass()).isEqualTo(64);
        assertThat(res.bookedSeats()).containsExactly("2C", "2D");
        assertThat(res.travelClass()).isEqualTo(TravelClass.AC_3);
    }

    @Test
    void seats_throwsWhenScheduleMissing() {
        UUID id = UUID.randomUUID();
        when(scheduleRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> scheduleService.seats(id, TravelClass.SLEEPER))
                .isInstanceOf(NotFoundException.class);
    }
}
