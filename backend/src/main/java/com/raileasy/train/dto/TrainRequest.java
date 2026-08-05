package com.raileasy.train.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Admin payload to create/update a train. Seats per class must be a multiple of 8
 * (8 columns A–H); the UI/grid assumes 8 rows × 8 = 64.
 */
public record TrainRequest(
        @NotBlank String trainNumber,
        @NotBlank String trainName,
        @Min(value = 8, message = "seatsPerClass must be at least 8")
        @Max(value = 64, message = "seatsPerClass must be at most 64")
        int seatsPerClass
) {
}
