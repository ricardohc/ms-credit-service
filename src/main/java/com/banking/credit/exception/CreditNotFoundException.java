package com.banking.credit.exception;

public class CreditNotFoundException extends DomainException {

    public CreditNotFoundException(String message) {
        super("CREDIT_NOT_FOUND", message);
    }
}