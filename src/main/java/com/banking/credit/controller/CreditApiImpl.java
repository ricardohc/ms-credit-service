package com.banking.credit.controller;

import com.banking.credit.api.CreditApi;
import com.banking.credit.dto.CreditCreateRequest;
import com.banking.credit.dto.CreditPage;
import com.banking.credit.dto.CreditResponse;
import com.banking.credit.dto.CreditUpdateRequest;
import com.banking.credit.mapper.CreditMapper;
import com.banking.credit.service.CreditService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CreditApiImpl implements CreditApi {

    private final CreditService creditService;
    private final CreditMapper creditMapper;

    @Override
    public Mono<ResponseEntity<CreditPage>> findAllCredits(String xCorrelationID, String xChannelID,
                                                           Integer page, Integer size,
                                                           ServerWebExchange exchange) {
        return creditService.findAll(page, size)
                .map(creditMapper::toResponse)
                .collectList()
                .map(items -> {
                    var p = new CreditPage();
                    p.setContent(items);
                    p.setPage(page);
                    p.setSize(size);
                    p.setTotalElements(items.size());
                    return ResponseEntity.ok(p);
                });
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> createCredit(String xCorrelationID, String xChannelID,
                                                             UUID xTransactionID,
                                                             Mono<CreditCreateRequest> body,
                                                             ServerWebExchange exchange) {
        return body.flatMap(req -> creditService.create(req.getCustomerId(),
                        req.getType().getValue(), new BigDecimal(req.getAmount()),
                        req.getCurrency(), req.getTermMonths(), xCorrelationID))
                .map(resp -> ResponseEntity.status(HttpStatus.CREATED).body(resp));
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> findCreditById(UUID creditId, String xCorrelationID,
                                                               String xChannelID,
                                                               ServerWebExchange exchange) {
        return creditService.findById(creditId)
                .map(creditMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CreditResponse>> updateCredit(UUID creditId, String xCorrelationID,
                                                             String xChannelID, UUID xTransactionID,
                                                             Mono<CreditUpdateRequest> body,
                                                             ServerWebExchange exchange) {
        return body.flatMap(req -> creditService.update(creditId, req, xCorrelationID))
                .map(creditMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}