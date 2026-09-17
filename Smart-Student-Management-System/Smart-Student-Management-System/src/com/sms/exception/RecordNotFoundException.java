package com.sms.exception;

/** Thrown when a requested record does not exist in the system. */
public class RecordNotFoundException extends SMSException {

    private static final long serialVersionUID = 1L;

    public RecordNotFoundException(String message) {
        super(message);
    }
}
