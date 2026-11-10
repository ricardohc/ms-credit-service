package com.banking.credit.service.validator.impl;

import com.banking.credit.exception.CreditConflictException;
import com.banking.credit.service.validator.CreditCreationValidator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CreditCreationValidatorImpl implements CreditCreationValidator {

    private static final String TYPE_PERSONAL = "PERSONAL";
    private static final String TYPE_BUSINESS = "BUSINESS";

    @Override
    public Mono<Void> validate(String type, boolean existsPersonal) {
        if (!TYPE_PERSONAL.equals(type) && !TYPE_BUSINESS.equals(type)) {
            return Mono.error(new CreditConflictException("Invalid credit type: " + type));
        }
        if (TYPE_PERSONAL.equals(type) && existsPersonal) {
            return Mono.error(new CreditConflictException(
                    "Customer already has a PERSONAL credit"));
        }
        return Mono.empty();
    }
}