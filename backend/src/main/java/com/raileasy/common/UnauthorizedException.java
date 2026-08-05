package com.raileasy.common;

/**
 * Thrown on failed authentication (e.g. bad credentials). Maps to HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
