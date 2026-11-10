package com.banking.credit.service.validator;

import com.banking.credit.dto.CreditUpdateRequest;
import com.banking.credit.document.CreditDocument;
import reactor.core.publisher.Mono;

public interface CreditUpdateValidator {
    Mono<Void> validate(CreditDocument current, CreditUpdateRequest request);
}