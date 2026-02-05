package com.example.exception;

public class PasswordRequiredException extends  RuntimeException{

    public PasswordRequiredException(String message) {
        super(message);
    }

    public PasswordRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
