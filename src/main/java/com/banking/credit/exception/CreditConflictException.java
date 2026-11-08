package com.banking.credit.exception;

/** Regla de negocio violada (ej. segundo credito PERSONAL) -> 409. */
public class CreditConflictException extends DomainException {

    public CreditConflictException(String message) {
        super("CREDIT_CONFLICT", message);
    }
}
