package com.banking.credit.document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Persistencia MongoDB separada de los DTOs de contrato (common-rules.md).
 * Dinero con BigDecimal, fechas en UTC.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "credits")
@CompoundIndex(name = "ux_customer_type", def = "{'customerId': 1, 'type': 1}", unique = false)
public class CreditDocument {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_OVERDUE = "OVERDUE";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @Id
    private UUID id;

    @Field("customerId")
    private UUID customerId;

    /** PERSONAL | BUSINESS */
    @Field("type")
    private String type;

    /** ACTIVE | PAID | OVERDUE | CANCELLED */
    @Field("status")
    private String status;

    @Field("amount")
    private BigDecimal amount;

    @Field("balance")
    private BigDecimal balance;

    @Field("currency")
    private String currency;

    @Field("termMonths")
    private Integer termMonths;

    @Field("dueDate")
    private LocalDate dueDate;

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedAt")
    private LocalDateTime updatedAt;

    /** Control de concurrencia. */
    @Version
    private Long version;
}
