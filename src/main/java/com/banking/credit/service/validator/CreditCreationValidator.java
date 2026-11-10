package com.banking.credit.service.validator;

import reactor.core.publisher.Mono;

public interface CreditCreationValidator {
    Mono<Void> validate(String type, boolean existsPersonal);
}