package com.banking.credit.event;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de dominio publicado en el topico banking.credit.events.v1 (Parte III).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditEvent {

    public static final String CREATED = "CreditCreated";
    public static final String UPDATED = "CreditUpdated";
    public static final String CANCELLED = "CreditCancelled";
    public static final String PAYMENT_APPLIED = "CreditPaymentApplied";
    public static final String OVERDUE_STATUS_CHANGED = "CreditOverdueStatusChanged";

    private String eventType;
    private UUID creditId;
    private UUID customerId;
    private String status;
    private String balance;
    private String currency;
    private Boolean hasOverdueDebt;
    private OffsetDateTime occurredAt;
    private String correlationId;
}
