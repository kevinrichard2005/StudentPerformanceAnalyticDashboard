package com.studentmanagement.exception;

/** Thrown when an authenticated user lacks permission for an action (HTTP 403). */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
