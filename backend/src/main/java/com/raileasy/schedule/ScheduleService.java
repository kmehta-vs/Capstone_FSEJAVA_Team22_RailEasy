package com.raileasy.schedule;

import com.raileasy.common.NotFoundException;
import com.raileasy.common.TravelClass;
import com.raileasy.schedule.dto.ClassAvailability;
import com.raileasy.schedule.dto.ScheduleRequest;
import com.raileasy.schedule.dto.ScheduleResponse;
import com.raileasy.schedule.dto.ScheduleSearchResponse;
import com.raileasy.schedule.dto.SeatAvailabilityResponse;
import com.raileasy.train.Train;
import com.raileasy.train.TrainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Search schedules and compute per-class availability using {@link BookedSeatsPort},
 * plus admin CRUD.
 */
@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TrainRepository trainRepository;
    private final BookedSeatsPort bookedSeatsPort;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           TrainRepository trainRepository,
                           BookedSeatsPort bookedSeatsPort) {
        this.scheduleRepository = scheduleRepository;
        this.trainRepository = trainRepository;
        this.bookedSeatsPort = bookedSeatsPort;
    }

    @Transactional(readOnly = true)
    public List<ScheduleSearchResponse> search(String from, String to, LocalDate date) {
        return scheduleRepository.search(from, to, date).stream()
                .map(this::toSearchResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeatAvailabilityResponse seats(UUID scheduleId, TravelClass travelClass) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found: " + scheduleId));
        List<String> booked = bookedSeatsPort.bookedSeats(scheduleId, travelClass);
        return new SeatAvailabilityResponse(
                scheduleId,
                travelClass,
                schedule.getTrain().getSeatsPerClass(),
                booked
        );
    }

    // --- Admin CRUD ---

    @Transactional(readOnly = true)
    public List<ScheduleResponse> findAll() {
        return scheduleRepository.findAll().stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @Transactional
    public ScheduleResponse create(ScheduleRequest request) {
        Train train = requireTrain(request.trainId());
        Schedule schedule = new Schedule(
                train,
                request.fromStation(),
                request.toStation(),
                request.departureTime(),
                request.arrivalTime(),
                request.journeyDate(),
                request.fareSleeper(),
                request.fareAc3(),
                request.fareAc2()
        );
        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    @Transactional
    public ScheduleResponse update(UUID id, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found: " + id));
        Train train = requireTrain(request.trainId());
        schedule.setTrain(train);
        schedule.setFromStation(request.fromStation());
        schedule.setToStation(request.toStation());
        schedule.setDepartureTime(request.departureTime());
        schedule.setArrivalTime(request.arrivalTime());
        schedule.setJourneyDate(request.journeyDate());
        schedule.setFareSleeper(request.fareSleeper());
        schedule.setFareAc3(request.fareAc3());
        schedule.setFareAc2(request.fareAc2());
        return ScheduleResponse.from(scheduleRepository.save(schedule));
    }

    @Transactional
    public void delete(UUID id) {
        if (!scheduleRepository.existsById(id)) {
            throw new NotFoundException("Schedule not found: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    private Train requireTrain(UUID trainId) {
        return trainRepository.findById(trainId)
                .orElseThrow(() -> new NotFoundException("Train not found: " + trainId));
    }

    private ScheduleSearchResponse toSearchResponse(Schedule s) {
        int seatsPerClass = s.getTrain().getSeatsPerClass();
        List<ClassAvailability> classes = List.of(
                availability(s, TravelClass.SLEEPER, s.getFareSleeper(), seatsPerClass),
                availability(s, TravelClass.AC_3, s.getFareAc3(), seatsPerClass),
                availability(s, TravelClass.AC_2, s.getFareAc2(), seatsPerClass)
        );
        return new ScheduleSearchResponse(
                s.getId(),
                s.getTrain().getTrainName(),
                s.getTrain().getTrainNumber(),
                s.getFromStation(),
                s.getToStation(),
                s.getDepartureTime(),
                s.getArrivalTime(),
                s.getJourneyDate(),
                classes
        );
    }

    private ClassAvailability availability(Schedule s, TravelClass tc, BigDecimal fare, int seatsPerClass) {
        int booked = bookedSeatsPort.bookedSeats(s.getId(), tc).size();
        int available = Math.max(0, seatsPerClass - booked);
        return new ClassAvailability(tc, fare, available);
    }
}
