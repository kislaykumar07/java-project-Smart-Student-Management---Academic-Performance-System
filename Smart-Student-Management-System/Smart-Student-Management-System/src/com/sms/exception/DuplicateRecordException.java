package com.sms.exception;

/** Thrown when a record with the same unique key already exists. */
public class DuplicateRecordException extends SMSException {

    private static final long serialVersionUID = 1L;

    public DuplicateRecordException(String message) {
        super(message);
    }
}
