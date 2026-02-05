package com.example.exception;

public class ShortCodeTakenException extends RuntimeException {

    public ShortCodeTakenException(String message) {
        super(message);
    }

    public ShortCodeTakenException(String message, Throwable cause) {
        super(message, cause);
    }
}
