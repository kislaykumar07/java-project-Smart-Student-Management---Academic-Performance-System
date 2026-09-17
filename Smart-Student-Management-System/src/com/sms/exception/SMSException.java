package com.sms.exception;

/**
 * Base checked exception for the Student Management System.
 * All application specific exceptions inherit from this class so that
 * callers can catch every domain error with a single catch block.
 */
public class SMSException extends Exception {

    private static final long serialVersionUID = 1L;

    public SMSException(String message) {
        super(message);
    }

    public SMSException(String message, Throwable cause) {
        super(message, cause);
    }
}
