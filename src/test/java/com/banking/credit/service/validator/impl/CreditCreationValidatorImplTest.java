package com.banking.credit.service.validator.impl;

import com.banking.credit.exception.CreditConflictException;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class CreditCreationValidatorImplTest {

    private final CreditCreationValidatorImpl validator = new CreditCreationValidatorImpl();

    @Test
    void personalFirstCreditIsAllowed() {
        StepVerifier.create(validator.validate("PERSONAL", false))
                .verifyComplete();
    }

    @Test
    void personalSecondCreditIsRejected() {
        StepVerifier.create(validator.validate("PERSONAL", true))
                .expectErrorMatches(e -> e instanceof CreditConflictException
                        && e.getMessage().contains("PERSONAL"))
                .verify();
    }

    @Test
    void businessIsAlwaysAllowed() {
        StepVerifier.create(validator.validate("BUSINESS", true))
                .verifyComplete();
        StepVerifier.create(validator.validate("BUSINESS", false))
                .verifyComplete();
    }

    @Test
    void invalidTypeIsRejected() {
        StepVerifier.create(validator.validate("MORTGAGE", false))
                .expectErrorMatches(e -> e instanceof CreditConflictException
                        && e.getMessage().contains("Invalid credit type"))
                .verify();
    }

    @Test
    void nullTypeIsRejected() {
        StepVerifier.create(validator.validate(null, false))
                .expectError(CreditConflictException.class)
                .verify();
    }
}