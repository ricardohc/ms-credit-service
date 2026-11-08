package com.banking.credit.exception;

/** Error al invocar a ms-transaction-service -> 502/500 segun contrato mapeado a 500. */
public class TransactionServiceException extends DomainException {

    public TransactionServiceException(String message) {
        super("TRANSACTION_SERVICE_ERROR", message);
    }
}
