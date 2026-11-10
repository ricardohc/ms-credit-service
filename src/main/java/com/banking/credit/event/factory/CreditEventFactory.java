package com.banking.credit.event.factory;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.event.CreditEvent;

public interface CreditEventFactory {
    CreditEvent insert(CreditDocument credit, String correlationId);
    CreditEvent update(CreditDocument credit, String correlationId);
    CreditEvent delete(CreditDocument credit, String correlationId);
}