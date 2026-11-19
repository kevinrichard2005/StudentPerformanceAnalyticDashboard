package com.studentmanagement.exception;

/** Thrown when request parameters are syntactically valid but semantically invalid (HTTP 400). */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
