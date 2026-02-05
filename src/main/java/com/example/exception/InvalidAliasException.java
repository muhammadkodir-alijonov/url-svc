package com.example.exception;

public class InvalidAliasException extends RuntimeException {

    public InvalidAliasException(String message) {
        super(message);
    }

    public InvalidAliasException(String message, Throwable cause) {
        super(message, cause);
    }
}