package com.sms.exception;

/** Thrown when user supplied data fails validation rules. */
public class InvalidInputException extends SMSException {

    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
