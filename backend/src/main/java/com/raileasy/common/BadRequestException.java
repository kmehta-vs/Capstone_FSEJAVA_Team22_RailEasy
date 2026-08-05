package com.raileasy.common;

/**
 * Thrown on invalid input beyond bean-validation (e.g. business rule violation).
 * Maps to HTTP 400.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
