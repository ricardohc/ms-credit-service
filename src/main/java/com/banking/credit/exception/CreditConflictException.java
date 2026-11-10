package com.banking.credit.exception;

public class CreditConflictException extends DomainException {

    public CreditConflictException(String message) {
        super("CREDIT_CONFLICT", message);
    }
}