package com.raileasy.dto;

import com.raileasy.enums.TravelClass;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassAvailability {
    private TravelClass travelClass;
    private BigDecimal fare;
    private int seatsAvailable;
}
