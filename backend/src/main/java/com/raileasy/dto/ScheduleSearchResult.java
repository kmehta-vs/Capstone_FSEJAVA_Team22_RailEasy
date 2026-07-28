package com.raileasy.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleSearchResult {
    private UUID scheduleId;
    private String trainName;
    private String trainNumber;
    private String fromStation;
    private String toStation;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private LocalDate journeyDate;
    private List<ClassAvailability> classAvailabilities;
}
