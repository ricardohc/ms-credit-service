package com.banking.credit.mapper;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.dto.CreditResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CreditMapper {

    public CreditDocument toNewDocument(UUID customerId, String type, BigDecimal amount,
                                        String currency, int termMonths) {
        var now = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        return CreditDocument.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .type(type)
                .status(CreditDocument.STATUS_ACTIVE)
                .amount(amount)
                .balance(amount)
                .currency(currency)
                .termMonths(termMonths)
                .dueDate(OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().plusMonths(termMonths))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public CreditResponse toResponse(CreditDocument doc) {
        var r = new CreditResponse();
        r.setId(doc.getId());
        r.setCustomerId(doc.getCustomerId());
        r.setType(com.banking.credit.dto.CreditType.fromValue(doc.getType()));
        r.setStatus(com.banking.credit.dto.CreditStatus.fromValue(doc.getStatus()));
        r.setAmount(doc.getAmount().toPlainString());
        r.setBalance(doc.getBalance().toPlainString());
        r.setCurrency(doc.getCurrency());
        r.setDueDate(doc.getDueDate());
        r.setCreatedAt(doc.getCreatedAt() == null ? null
                : doc.getCreatedAt().atOffset(ZoneOffset.UTC));
        return r;
    }
}