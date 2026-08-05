package com.raileasy.schedule.dto;

import com.raileasy.common.TravelClass;

import java.math.BigDecimal;

/**
 * Availability + fare for a single travel class within a schedule search result.
 */
public record ClassAvailability(
        TravelClass travelClass,
        BigDecimal fare,
        int seatsAvailable
) {
}
