package com.raileasy.booking.dto;

import com.raileasy.booking.Booking;
import com.raileasy.booking.BookingStatus;
import com.raileasy.common.TravelClass;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A booking as shown on the confirmation screen and "My Tickets" table.
 */
public record BookingResponse(
        UUID id,
        String pnrNumber,
        UUID scheduleId,
        String trainName,
        String trainNumber,
        String fromStation,
        String toStation,
        LocalDate journeyDate,
        TravelClass travelClass,
        List<String> seatNumbers,
        BigDecimal farePerSeat,
        BigDecimal totalFare,
        BookingStatus status,
        Instant createdAt
) {
    public static BookingResponse from(Booking b, BigDecimal farePerSeat) {
        List<String> seats = b.seatList();
        BigDecimal total = farePerSeat.multiply(BigDecimal.valueOf(seats.size()));
        var s = b.getSchedule();
        return new BookingResponse(
                b.getId(),
                b.getPnrNumber(),
                s.getId(),
                s.getTrain().getTrainName(),
                s.getTrain().getTrainNumber(),
                s.getFromStation(),
                s.getToStation(),
                s.getJourneyDate(),
                b.getTravelClass(),
                seats,
                farePerSeat,
                total,
                b.getStatus(),
                b.getCreatedAt()
        );
    }
}
