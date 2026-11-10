package com.banking.credit.service.validator.impl;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.dto.CreditStatus;
import com.banking.credit.dto.CreditUpdateRequest;
import com.banking.credit.exception.CreditConflictException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class CreditUpdateValidatorImplTest {

    private final CreditUpdateValidatorImpl validator = new CreditUpdateValidatorImpl();

    private CreditDocument doc(BigDecimal balance) {
        return CreditDocument.builder()
                .id(java.util.UUID.randomUUID())
                .balance(balance)
                .status("ACTIVE")
                .build();
    }

    private CreditUpdateRequest req(CreditStatus status) {
        var r = new CreditUpdateRequest();
        r.setStatus(status);
        return r;
    }

    @Test
    void nullRequestIsAllowed() {
        StepVerifier.create(validator.validate(doc(BigDecimal.TEN), null))
                .verifyComplete();
    }

    @Test
    void requestWithoutStatusIsAllowed() {
        StepVerifier.create(validator.validate(doc(BigDecimal.TEN), new CreditUpdateRequest()))
                .verifyComplete();
    }

    @Test
    void paidWithPositiveBalanceIsRejected() {
        StepVerifier.create(validator.validate(doc(new BigDecimal("0.01")), req(CreditStatus.PAID)))
                .expectError(CreditConflictException.class)
                .verify();
    }

    @Test
    void paidWithZeroBalanceIsAllowed() {
        StepVerifier.create(validator.validate(doc(BigDecimal.ZERO), req(CreditStatus.PAID)))
                .verifyComplete();
    }

    @Test
    void cancelledWithPositiveBalanceIsAllowed() {
        StepVerifier.create(validator.validate(doc(new BigDecimal("500.00")),
                        req(CreditStatus.CANCELLED)))
                .verifyComplete();
    }

    @Test
    void overdueWithPositiveBalanceIsAllowed() {
        StepVerifier.create(validator.validate(doc(new BigDecimal("500.00")),
                        req(CreditStatus.OVERDUE)))
                .verifyComplete();
    }

    @Test
    void activeStatusIsAlwaysAllowed() {
        StepVerifier.create(validator.validate(doc(new BigDecimal("500.00")),
                        req(CreditStatus.ACTIVE)))
                .verifyComplete();
    }
}