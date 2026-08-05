package com.raileasy.train;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A physical train with a fixed number of seats per travel class.
 * Train (1) -- (M) Schedule.
 */
@Entity
@Table(name = "train")
public class Train {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "train_number", nullable = false, unique = true)
    private String trainNumber;

    @Column(name = "train_name", nullable = false)
    private String trainName;

    @Column(name = "seats_per_class", nullable = false)
    private int seatsPerClass = 64;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Train() {
        // JPA
    }

    public Train(String trainNumber, String trainName, int seatsPerClass) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.seatsPerClass = seatsPerClass;
    }

    public UUID getId() {
        return id;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public int getSeatsPerClass() {
        return seatsPerClass;
    }

    public void setSeatsPerClass(int seatsPerClass) {
        this.seatsPerClass = seatsPerClass;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
