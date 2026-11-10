package com.banking.credit.service.impl;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.dto.CreditResponse;
import com.banking.credit.dto.CreditUpdateRequest;
import com.banking.credit.event.CreditEventPublisher;
import com.banking.credit.event.factory.CreditEventFactory;
import com.banking.credit.exception.CreditNotFoundException;
import com.banking.credit.mapper.CreditMapper;
import com.banking.credit.repository.CreditRepository;
import com.banking.credit.service.CreditService;
import com.banking.credit.service.validator.CreditCreationValidator;
import com.banking.credit.service.validator.CreditUpdateValidator;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    public static final String TYPE_PERSONAL = "PERSONAL";

    private final CreditRepository creditRepository;
    private final CreditMapper creditMapper;
    private final CreditEventPublisher eventPublisher;
    private final CreditEventFactory eventFactory;
    private final CreditCreationValidator creationValidator;
    private final CreditUpdateValidator updateValidator;

    @Override
    public Mono<CreditResponse> create(UUID customerId, String type, BigDecimal amount,
                                       String currency, Integer termMonths, String correlationId) {
        return creditRepository.existsByCustomerIdAndType(customerId, type)
                .flatMap(exists -> creationValidator.validate(type, Boolean.TRUE.equals(exists)))
                .then(Mono.defer(() -> creditRepository.save(
                        creditMapper.toNewDocument(customerId, type, amount, currency, termMonths))))
                .flatMap(saved -> eventPublisher.publish(eventFactory.insert(saved, correlationId))
                        .onErrorResume(err -> {
                            log.warn("No se pudo publicar evento INSERT para credito {}: {}",
                                    saved.getId(), err.getMessage());
                            return Mono.empty();
                        })
                        .thenReturn(saved))
                .map(creditMapper::toResponse);
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
    public Mono<CreditDocument> update(UUID creditId, CreditUpdateRequest request, String correlationId) {
        return findById(creditId)
                .flatMap(doc -> updateValidator.validate(doc, request).thenReturn(doc))
                .flatMap(doc -> {
                    if (request == null || request.getStatus() == null) {
                        return Mono.just(doc);
                    }
                    doc.setStatus(request.getStatus().getValue());
                    doc.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
                    return creditRepository.save(doc)
                            .flatMap(saved -> eventPublisher.publish(
                                            eventFactory.update(saved, correlationId))
                                    .onErrorResume(err -> {
                                        log.warn("No se pudo publicar evento UPDATE para credito {}: {}",
                                                saved.getId(), err.getMessage());
                                        return Mono.empty();
                                    })
                                    .thenReturn(saved));
                });
    }
}