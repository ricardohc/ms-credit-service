package com.banking.credit.exception;

/** Pago mayor que la deuda pendiente (no hay saldo a favor) -> 422. */
public class PaymentExceedsBalanceException extends DomainException {

    public PaymentExceedsBalanceException(String message) {
        super("PAYMENT_EXCEEDS_BALANCE", message);
    }
}
