package com.raileasy.train.dto;

import java.util.UUID;

/**
 * Public representation of a train.
 */
public record TrainResponse(
        UUID id,
        String trainNumber,
        String trainName,
        int seatsPerClass
) {
}
