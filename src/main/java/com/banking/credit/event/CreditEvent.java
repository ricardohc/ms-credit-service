package com.banking.credit.event;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditEvent {

    public static final String INSERT = "i";
    public static final String UPDATE = "u";
    public static final String DELETE = "d";

    private UUID eventId;
    private String eventType;
    private Instant occurredAt;
    private String correlationId;
    private CreditPayload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditPayload {
        private UUID creditId;
        private UUID customerId;
        private String type;
        private String status;
        private String amount;
        private String balance;
        private String currency;
        private Integer termMonths;
        private String dueDate;
        private String createdAt;
        private String updatedAt;
    }
}