package com.pinnacle.backend.exceptions;

public class HeaderValidationException extends IllegalArgumentException {
    public HeaderValidationException(String message) {
        super(message);
    }
}
