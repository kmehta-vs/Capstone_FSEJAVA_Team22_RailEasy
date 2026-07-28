package com.raileasy.service;

import com.raileasy.dto.ClassAvailability;
import com.raileasy.dto.ScheduleSearchResult;
import com.raileasy.entity.Booking;
import com.raileasy.entity.Schedule;
import com.raileasy.enums.BookingStatus;
import com.raileasy.enums.TravelClass;
import com.raileasy.repository.BookingRepository;
import com.raileasy.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;

    public List<ScheduleSearchResult> searchSchedules(String from, String to, LocalDate date) {
        List<Schedule> schedules = scheduleRepository.searchSchedules(from, to, date);
        return schedules.stream()
                .map(this::toSearchResult)
                .toList();
    }

    private ScheduleSearchResult toSearchResult(Schedule schedule) {
        int seatsPerClass = schedule.getTrain().getSeatsPerClass();

        List<ClassAvailability> classAvailabilities = Arrays.stream(TravelClass.values())
                .map(tc -> ClassAvailability.builder()
                        .travelClass(tc)
                        .fare(getFare(schedule, tc))
                        .seatsAvailable(seatsPerClass - getBookedSeatCount(schedule.getId(), tc))
                        .build())
                .toList();

        return ScheduleSearchResult.builder()
                .scheduleId(schedule.getId())
                .trainName(schedule.getTrain().getTrainName())
                .trainNumber(schedule.getTrain().getTrainNumber())
                .fromStation(schedule.getFromStation())
                .toStation(schedule.getToStation())
                .departureTime(schedule.getDepartureTime())
                .arrivalTime(schedule.getArrivalTime())
                .journeyDate(schedule.getJourneyDate())
                .classAvailabilities(classAvailabilities)
                .build();
    }

    private BigDecimal getFare(Schedule schedule, TravelClass travelClass) {
        return switch (travelClass) {
            case SLEEPER -> schedule.getFareSleeper();
            case AC_3 -> schedule.getFareAc3();
            case AC_2 -> schedule.getFareAc2();
        };
    }

    private int getBookedSeatCount(java.util.UUID scheduleId, TravelClass travelClass) {
        List<Booking> bookings = bookingRepository.findByScheduleIdAndTravelClassAndStatus(
                scheduleId, travelClass, BookingStatus.CONFIRMED);
        return bookings.stream()
                .mapToInt(b -> b.getSeatNumbers().split(",").length)
                .sum();
    }
}
