package com.banking.credit.event.factory.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.event.CreditEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreditEventFactoryImplTest {

    private final CreditEventFactoryImpl factory = new CreditEventFactoryImpl();

    private CreditDocument doc() {
        return CreditDocument.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .type("PERSONAL")
                .status("ACTIVE")
                .amount(new BigDecimal("1000.00"))
                .balance(new BigDecimal("900.00"))
                .currency("PEN")
                .termMonths(12)
                .dueDate(LocalDate.of(2025, 6, 1))
                .createdAt(LocalDateTime.of(2024, 6, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2024, 6, 2, 13, 0))
                .build();
    }

    @Test
    void insertBuildsInsertEvent() {
        var credit = doc();
        var event = factory.insert(credit, "corr-1");

        assertEquals(CreditEvent.INSERT, event.getEventType());
        assertEquals("corr-1", event.getCorrelationId());
        assertNotNull(event.getEventId());
        assertNotNull(event.getOccurredAt());
        assertEquals(credit.getId(), event.getPayload().getCreditId());
        assertEquals(credit.getCustomerId(), event.getPayload().getCustomerId());
        assertEquals("PERSONAL", event.getPayload().getType());
        assertEquals("ACTIVE", event.getPayload().getStatus());
        assertEquals("1000.00", event.getPayload().getAmount());
        assertEquals("900.00", event.getPayload().getBalance());
        assertEquals("PEN", event.getPayload().getCurrency());
        assertEquals(12, event.getPayload().getTermMonths());
        assertEquals("2025-06-01", event.getPayload().getDueDate());
        assertNotNull(event.getPayload().getCreatedAt());
        assertNotNull(event.getPayload().getUpdatedAt());
    }

    @Test
    void updateBuildsUpdateEvent() {
        var event = factory.update(doc(), "corr-2");

        assertEquals(CreditEvent.UPDATE, event.getEventType());
        assertEquals("corr-2", event.getCorrelationId());
    }

    @Test
    void deleteBuildsDeleteEvent() {
        var event = factory.delete(doc(), "corr-3");

        assertEquals(CreditEvent.DELETE, event.getEventType());
        assertEquals("corr-3", event.getCorrelationId());
    }

    @Test
    void handlesNullDueDateAndTimestamps() {
        var credit = doc();
        credit.setDueDate(null);
        credit.setCreatedAt(null);
        credit.setUpdatedAt(null);

        var event = factory.insert(credit, "corr");

        assertNull(event.getPayload().getDueDate());
        assertNull(event.getPayload().getCreatedAt());
        assertNull(event.getPayload().getUpdatedAt());
    }

    @Test
    void eachEventGetsUniqueEventId() {
        var credit = doc();
        var e1 = factory.insert(credit, "c");
        var e2 = factory.insert(credit, "c");

        org.junit.jupiter.api.Assertions.assertNotEquals(e1.getEventId(), e2.getEventId());
    }
}