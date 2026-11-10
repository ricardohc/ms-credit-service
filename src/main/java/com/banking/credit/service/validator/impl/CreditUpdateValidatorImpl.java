package com.banking.credit.service.validator.impl;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.dto.CreditUpdateRequest;
import com.banking.credit.exception.CreditConflictException;
import com.banking.credit.service.validator.CreditUpdateValidator;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CreditUpdateValidatorImpl implements CreditUpdateValidator {

    @Override
    public Mono<Void> validate(CreditDocument current, CreditUpdateRequest request) {
        if (request == null || request.getStatus() == null) {
            return Mono.empty();
        }
        String newStatus = request.getStatus().getValue();
        if (CreditDocument.STATUS_PAID.equals(newStatus)
                && current.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            return Mono.error(new CreditConflictException(
                    "Cannot set PAID while balance is greater than zero"));
        }
        return Mono.empty();
    }
}