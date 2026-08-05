package com.raileasy.common;

/**
 * Thrown on conflicting state changes (e.g. seat already booked). Maps to HTTP 409.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
