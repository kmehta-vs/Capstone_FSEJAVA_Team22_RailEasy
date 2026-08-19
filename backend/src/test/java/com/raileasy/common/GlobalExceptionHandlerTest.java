package com.raileasy.common;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/trains");
    }

    @Test
    void handleNotFound_returns404WithNotFoundCode() {
        ResponseEntity<ApiError> response = handler.handleNotFound(new NotFoundException("Train not found"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().error()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("Train not found");
        assertThat(response.getBody().path()).isEqualTo("/api/trains");
    }

    @Test
    void handleConflict_returns409WithConflictCode() {
        ResponseEntity<ApiError> response = handler.handleConflict(new ConflictException("Seat already booked"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().error()).isEqualTo("CONFLICT");
    }

    @Test
    void handleBadRequest_returns400WithValidationErrorCode() {
        ResponseEntity<ApiError> response = handler.handleBadRequest(new BadRequestException("Invalid seats"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().error()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void handleUnauthorized_returns401WithUnauthorizedCode() {
        ResponseEntity<ApiError> response = handler.handleUnauthorized(new UnauthorizedException("Bad credentials"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().error()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    void handleAccessDenied_returns403WithForbiddenCode() {
        ResponseEntity<ApiError> response = handler.handleAccessDenied(new AccessDeniedException("denied"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().error()).isEqualTo("FORBIDDEN");
        assertThat(response.getBody().message()).isEqualTo("Access denied");
    }

    @Test
    void handleGeneric_returns500WithInternalErrorCode() {
        ResponseEntity<ApiError> response = handler.handleGeneric(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().error()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).isEqualTo("boom");
    }
}
