package com.banking.credit.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.banking.credit.document.CreditDocument;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreditMapperTest {

    private final CreditMapper mapper = new CreditMapper();

    @Test
    void toNewDocumentSetsDefaultValues() {
        var customerId = UUID.randomUUID();
        var doc = mapper.toNewDocument(customerId, "PERSONAL",
                new BigDecimal("1500.00"), "PEN", 12);

        assertNotNull(doc.getId());
        assertEquals(customerId, doc.getCustomerId());
        assertEquals("PERSONAL", doc.getType());
        assertEquals(CreditDocument.STATUS_ACTIVE, doc.getStatus());
        assertEquals(new BigDecimal("1500.00"), doc.getAmount());
        assertEquals(new BigDecimal("1500.00"), doc.getBalance());
        assertEquals("PEN", doc.getCurrency());
        assertEquals(12, doc.getTermMonths());
        assertNotNull(doc.getDueDate());
        assertNotNull(doc.getCreatedAt());
        assertNotNull(doc.getUpdatedAt());
    }

    @Test
    void toNewDocumentDueDateIsTermMonthsAhead() {
        var expectedDueDate = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().plusMonths(6);

        var doc = mapper.toNewDocument(UUID.randomUUID(), "BUSINESS",
                new BigDecimal("100.00"), "USD", 6);

        assertEquals(expectedDueDate, doc.getDueDate());
    }

    @Test
    void toResponseMapsAllFields() {
        var id = UUID.randomUUID();
        var customerId = UUID.randomUUID();
        var createdAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        var doc = CreditDocument.builder()
                .id(id)
                .customerId(customerId)
                .type("BUSINESS")
                .status("PAID")
                .amount(new BigDecimal("500.00"))
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .termMonths(24)
                .dueDate(LocalDate.of(2026, 1, 15))
                .createdAt(createdAt)
                .build();

        var response = mapper.toResponse(doc);

        assertEquals(id, response.getId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals("BUSINESS", response.getType().getValue());
        assertEquals("PAID", response.getStatus().getValue());
        assertEquals("500.00", response.getAmount());
        assertEquals("0", response.getBalance());
        assertEquals("USD", response.getCurrency());
        assertEquals(LocalDate.of(2026, 1, 15), response.getDueDate());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void toResponseHandlesNullCreatedAt() {
        var doc = CreditDocument.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .type("PERSONAL")
                .status("ACTIVE")
                .amount(new BigDecimal("100.00"))
                .balance(new BigDecimal("100.00"))
                .currency("PEN")
                .termMonths(12)
                .dueDate(LocalDate.now())
                .createdAt(null)
                .build();

        var response = mapper.toResponse(doc);

        assertNull(response.getCreatedAt());
    }
}