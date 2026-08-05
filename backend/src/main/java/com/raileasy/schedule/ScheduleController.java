package com.raileasy.schedule;

import com.raileasy.common.TravelClass;
import com.raileasy.schedule.dto.ScheduleRequest;
import com.raileasy.schedule.dto.ScheduleResponse;
import com.raileasy.schedule.dto.ScheduleSearchResponse;
import com.raileasy.schedule.dto.SeatAvailabilityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@Tag(name = "Schedules", description = "Search schedules, view seat availability, admin CRUD")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    @Operation(summary = "Search schedules by from/to station and date, with class availability and fares")
    public List<ScheduleSearchResponse> search(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return scheduleService.search(from, to, date);
    }

    @GetMapping("/{id}/seats")
    @Operation(summary = "Booked seat numbers for a schedule and travel class")
    public SeatAvailabilityResponse seats(
            @PathVariable("id") UUID id,
            @RequestParam("travelClass") TravelClass travelClass) {
        return scheduleService.seats(id, travelClass);
    }

    // --- Admin CRUD ---

    @GetMapping("/all")
    @Operation(summary = "List all schedules (admin management view)")
    @SecurityRequirement(name = "bearerAuth")
    public List<ScheduleResponse> findAll() {
        return scheduleService.findAll();
    }

    @PostMapping
    @Operation(summary = "Create a schedule (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ScheduleResponse> create(@Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a schedule (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ScheduleResponse update(@PathVariable("id") UUID id, @Valid @RequestBody ScheduleRequest request) {
        return scheduleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a schedule (admin)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
