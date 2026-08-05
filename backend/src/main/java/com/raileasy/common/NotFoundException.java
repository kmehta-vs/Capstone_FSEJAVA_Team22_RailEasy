package com.raileasy.common;

/**
 * Thrown when a requested entity cannot be found. Maps to HTTP 404.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
