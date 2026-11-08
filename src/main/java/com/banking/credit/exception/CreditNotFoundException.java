package com.banking.credit.exception;

/** Recurso no encontrado -> 404. */
public class CreditNotFoundException extends DomainException {

    public CreditNotFoundException(String message) {
        super("CREDIT_NOT_FOUND", message);
    }
}
