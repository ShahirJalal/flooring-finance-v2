package com.flooring.finance.exception;

/** Thrown when a username has failed login too many times recently - see {@code LoginRateLimiter}. */
public class TooManyAttemptsException extends RuntimeException {
    public TooManyAttemptsException(String message) {
        super(message);
    }
}
