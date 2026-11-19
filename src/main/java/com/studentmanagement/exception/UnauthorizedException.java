package com.studentmanagement.exception;

/** Thrown when authentication fails or a valid token is missing (HTTP 401). */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
