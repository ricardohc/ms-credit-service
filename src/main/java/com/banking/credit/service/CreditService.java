package com.banking.credit.service;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.dto.CreditResponse;
import com.banking.credit.dto.CreditUpdateRequest;
import java.math.BigDecimal;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditService {

    Mono<CreditResponse> create(UUID customerId, String type, BigDecimal amount,
                                String currency, Integer termMonths, String correlationId);

    Flux<CreditDocument> findAll(int page, int size);

    Mono<CreditDocument> findById(UUID creditId);

    Mono<CreditDocument> update(UUID creditId, CreditUpdateRequest request, String correlationId);
}