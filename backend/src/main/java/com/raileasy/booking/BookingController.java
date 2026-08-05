package com.raileasy.booking;

import com.raileasy.booking.dto.BookingResponse;
import com.raileasy.booking.dto.CreateBookingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Create, view and cancel bookings")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @Operation(summary = "Create a booking (1–4 seats) and generate a PNR")
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {
        BookingResponse response = bookingService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    @Operation(summary = "List the current user's bookings")
    public List<BookingResponse> mine(Authentication authentication) {
        return bookingService.mine(authentication.getName());
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking (frees the seats)")
    public BookingResponse cancel(
            @PathVariable("id") UUID id,
            Authentication authentication) {
        return bookingService.cancel(authentication.getName(), id);
    }
}
