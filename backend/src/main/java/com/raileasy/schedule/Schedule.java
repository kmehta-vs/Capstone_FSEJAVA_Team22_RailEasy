package com.raileasy.schedule;

import com.raileasy.train.Train;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A dated run of a {@link Train} between two stations with per-class fares.
 * Schedule (M) -- (1) Train, and Schedule (1) -- (M) Booking.
 */
@Entity
@Table(name = "schedule")
public class Schedule {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(name = "from_station", nullable = false)
    private String fromStation;

    @Column(name = "to_station", nullable = false)
    private String toStation;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    @Column(name = "fare_sleeper", nullable = false)
    private BigDecimal fareSleeper;

    @Column(name = "fare_ac3", nullable = false)
    private BigDecimal fareAc3;

    @Column(name = "fare_ac2", nullable = false)
    private BigDecimal fareAc2;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Schedule() {
        // JPA
    }

    public Schedule(Train train, String fromStation, String toStation,
                    LocalDateTime departureTime, LocalDateTime arrivalTime, LocalDate journeyDate,
                    BigDecimal fareSleeper, BigDecimal fareAc3, BigDecimal fareAc2) {
        this.train = train;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.journeyDate = journeyDate;
        this.fareSleeper = fareSleeper;
        this.fareAc3 = fareAc3;
        this.fareAc2 = fareAc2;
    }

    public UUID getId() {
        return id;
    }

    public Train getTrain() {
        return train;
    }

    public void setTrain(Train train) {
        this.train = train;
    }

    public String getFromStation() {
        return fromStation;
    }

    public void setFromStation(String fromStation) {
        this.fromStation = fromStation;
    }

    public String getToStation() {
        return toStation;
    }

    public void setToStation(String toStation) {
        this.toStation = toStation;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public LocalDate getJourneyDate() {
        return journeyDate;
    }

    public void setJourneyDate(LocalDate journeyDate) {
        this.journeyDate = journeyDate;
    }

    public BigDecimal getFareSleeper() {
        return fareSleeper;
    }

    public void setFareSleeper(BigDecimal fareSleeper) {
        this.fareSleeper = fareSleeper;
    }

    public BigDecimal getFareAc3() {
        return fareAc3;
    }

    public void setFareAc3(BigDecimal fareAc3) {
        this.fareAc3 = fareAc3;
    }

    public BigDecimal getFareAc2() {
        return fareAc2;
    }

    public void setFareAc2(BigDecimal fareAc2) {
        this.fareAc2 = fareAc2;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
