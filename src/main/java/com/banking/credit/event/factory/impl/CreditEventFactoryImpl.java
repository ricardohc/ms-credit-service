package com.banking.credit.event.factory.impl;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.event.CreditEvent;
import com.banking.credit.event.factory.CreditEventFactory;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CreditEventFactoryImpl implements CreditEventFactory {

    @Override
    public CreditEvent insert(CreditDocument credit, String correlationId) {
        return build(CreditEvent.INSERT, credit, correlationId);
    }

    @Override
    public CreditEvent update(CreditDocument credit, String correlationId) {
        return build(CreditEvent.UPDATE, credit, correlationId);
    }

    @Override
    public CreditEvent delete(CreditDocument credit, String correlationId) {
        return build(CreditEvent.DELETE, credit, correlationId);
    }

    private CreditEvent build(String eventType, CreditDocument credit, String correlationId) {
        return CreditEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .occurredAt(Instant.now())
                .correlationId(correlationId)
                .payload(CreditEvent.CreditPayload.builder()
                        .creditId(credit.getId())
                        .customerId(credit.getCustomerId())
                        .type(credit.getType())
                        .status(credit.getStatus())
                        .amount(credit.getAmount().toPlainString())
                        .balance(credit.getBalance().toPlainString())
                        .currency(credit.getCurrency())
                        .termMonths(credit.getTermMonths())
                        .dueDate(credit.getDueDate() == null ? null : credit.getDueDate().toString())
                        .createdAt(credit.getCreatedAt() == null ? null
                                : credit.getCreatedAt().toInstant(ZoneOffset.UTC).toString())
                        .updatedAt(credit.getUpdatedAt() == null ? null
                                : credit.getUpdatedAt().toInstant(ZoneOffset.UTC).toString())
                        .build())
                .build();
    }
}