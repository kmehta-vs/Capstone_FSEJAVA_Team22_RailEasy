package com.raileasy.booking;

import com.raileasy.common.TravelClass;
import com.raileasy.schedule.Schedule;
import com.raileasy.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A confirmed (or cancelled) seat reservation for a schedule + travel class.
 * Booking (M) -- (1) User, Booking (M) -- (1) Schedule.
 *
 * <p>Seat labels are stored as a CSV string (e.g. {@code "1A,1B"}).
 */
@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_class", nullable = false)
    private TravelClass travelClass;

    @Column(name = "seat_numbers", nullable = false)
    private String seatNumbers;

    @Column(name = "pnr_number", nullable = false, unique = true)
    private String pnrNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Booking() {
        // JPA
    }

    public Booking(User user, Schedule schedule, TravelClass travelClass,
                   List<String> seatNumbers, String pnrNumber) {
        this.user = user;
        this.schedule = schedule;
        this.travelClass = travelClass;
        this.seatNumbers = String.join(",", seatNumbers);
        this.pnrNumber = pnrNumber;
        this.status = BookingStatus.CONFIRMED;
    }

    /** Seat labels for this booking, split from the stored CSV. */
    public List<String> seatList() {
        if (seatNumbers == null || seatNumbers.isBlank()) {
            return List.of();
        }
        return List.of(seatNumbers.split(","));
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public TravelClass getTravelClass() {
        return travelClass;
    }

    public String getSeatNumbers() {
        return seatNumbers;
    }

    public String getPnrNumber() {
        return pnrNumber;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
