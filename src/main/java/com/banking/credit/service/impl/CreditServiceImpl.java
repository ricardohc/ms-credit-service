package com.banking.credit.service.impl;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.event.CreditEvent;
import com.banking.credit.dto.CreditResponse;
import com.banking.credit.event.CreditEventPublisher;
import com.banking.credit.exception.CreditConflictException;
import com.banking.credit.exception.CreditNotFoundException;
import com.banking.credit.mapper.CreditMapper;
import com.banking.credit.repository.CreditRepository;
import com.banking.credit.service.CreditService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementacion reactiva (Mono/Flux) de las reglas de negocio de creditos.
 * Transiciones financieras (PAID, OVERDUE) solo por pagos o procesos automaticos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    public static final String TYPE_PERSONAL = "PERSONAL";
    public static final String TYPE_BUSINESS = "BUSINESS";

    private final CreditRepository creditRepository;
    private final CreditMapper creditMapper;
    private final CreditEventPublisher eventPublisher;

    @Override
    public Mono<CreditResponse> create(UUID customerId, String type, BigDecimal amount,
                                       String currency, Integer termMonths, String correlationId) {
        return validateType(type)
                .then(creditRepository.findByCustomerIdAndType(customerId, type)
                        .hasElement()
                        .flatMap(exists -> {
                            if (TYPE_PERSONAL.equals(type) && Boolean.TRUE.equals(exists)) {
                                return Mono.error(new CreditConflictException(
                                        "Customer already has a PERSONAL credit"));
                            }
                            return Mono.just(false);
                        })
                        .then(Mono.defer(() -> {
                            var doc = creditMapper.toNewDocument(customerId, type, amount,
                                    currency, termMonths);
                            return creditRepository.save(doc)
                                    .flatMap(saved -> eventPublisher.publish(CreditEvent.builder()
                                                    .eventType(CreditEvent.CREATED)
                                                    .creditId(saved.getId())
                                                    .customerId(saved.getCustomerId())
                                                    .status(saved.getStatus())
                                                    .balance(saved.getBalance().toPlainString())
                                                    .currency(saved.getCurrency())
                                                    .occurredAt(OffsetDateTime.now(ZoneOffset.UTC))
                                                    .correlationId(correlationId)
                                                    .build())
                                            .thenReturn(saved))
                                    .map(creditMapper::toResponse);
                        })));
    }

    @Override
    public Flux<CreditDocument> findAll(int page, int size) {
        return creditRepository.findAll()
                .skip((long) page * size)
                .take(size);
    }

    @Override
    public Mono<CreditDocument> findById(UUID creditId) {
        return creditRepository.findById(creditId)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(
                        "Credit not found: " + creditId)));
    }

    @Override
    public Mono<CreditDocument> update(UUID creditId, com.banking.credit.dto.CreditUpdateRequest request,
                                       String correlationId) {
        return findById(creditId)
                .flatMap(doc -> {
                    var newStatus = request.getStatus() == null ? null : request.getStatus().getValue();
                    if (newStatus == null) {
                        return Mono.just(doc);
                    }
                    // No se permite PAID por PATCH si balance > 0 (regla de negocio).
                    if (CreditDocument.STATUS_PAID.equals(newStatus)
                            && doc.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                        return Mono.error(new CreditConflictException(
                                "Cannot set PAID while balance is greater than zero"));
                    }
                    doc.setStatus(newStatus);
                    doc.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
                    return creditRepository.save(doc)
                            .flatMap(saved -> eventPublisher.publish(CreditEvent.builder()
                                            .eventType(CreditEvent.UPDATED)
                                            .creditId(saved.getId())
                                            .customerId(saved.getCustomerId())
                                            .status(saved.getStatus())
                                            .balance(saved.getBalance().toPlainString())
                                            .currency(saved.getCurrency())
                                            .occurredAt(OffsetDateTime.now(ZoneOffset.UTC))
                                            .correlationId(correlationId)
                                            .build())
                                    .thenReturn(saved));
                });
    }

    @Override
    public Mono<Void> deactivate(UUID creditId, String correlationId) {
        return findById(creditId)
                .flatMap(doc -> {
                    doc.setStatus(CreditDocument.STATUS_CANCELLED);
                    doc.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
                    return creditRepository.save(doc)
                            .flatMap(saved -> eventPublisher.publish(CreditEvent.builder()
                                            .eventType(CreditEvent.CANCELLED)
                                            .creditId(saved.getId())
                                            .customerId(saved.getCustomerId())
                                            .status(saved.getStatus())
                                            .balance(saved.getBalance().toPlainString())
                                            .currency(saved.getCurrency())
                                            .occurredAt(OffsetDateTime.now(ZoneOffset.UTC))
                                            .correlationId(correlationId)
                                            .build())
                                    .thenReturn(saved));
                })
                .then();
    }

    @Override
    public Mono<OverdueInfo> getOverdueInfo(UUID creditId) {
        return findById(creditId)
                .map(doc -> {
                    if (doc.getDueDate() == null || doc.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                        return new OverdueInfo(false, BigDecimal.ZERO, 0L);
                    }
                    long days = ChronoUnit.DAYS.between(doc.getDueDate(),
                            OffsetDateTime.now(ZoneOffset.UTC).toLocalDate());
                    if (days <= 0) {
                        return new OverdueInfo(false, BigDecimal.ZERO, 0L);
                    }
                    return new OverdueInfo(true, doc.getBalance(), days);
                });
    }

    private Mono<Void> validateType(String type) {
        if (!TYPE_PERSONAL.equals(type) && !TYPE_BUSINESS.equals(type)) {
            return Mono.error(new CreditConflictException("Invalid credit type: " + type));
        }
        return Mono.empty();
    }
}