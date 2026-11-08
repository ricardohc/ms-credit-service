package com.banking.credit.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.dto.CreditResponse;
import com.banking.credit.dto.CreditStatus;
import com.banking.credit.dto.CreditUpdateRequest;
import com.banking.credit.event.CreditEventPublisher;
import com.banking.credit.exception.CreditConflictException;
import com.banking.credit.exception.CreditNotFoundException;
import com.banking.credit.mapper.CreditMapper;
import com.banking.credit.repository.CreditRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CreditServiceImplTest {

    private CreditRepository repository;
    private CreditEventPublisher eventPublisher;
    private CreditServiceImpl service;

    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(CreditRepository.class);
        eventPublisher = Mockito.mock(CreditEventPublisher.class);
        Mockito.when(eventPublisher.publish(any())).thenReturn(Mono.empty());
        service = new CreditServiceImpl(repository, new CreditMapper(), eventPublisher);
    }

    private CreditDocument doc(String type, BigDecimal balance, LocalDate dueDate) {
        return CreditDocument.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .type(type)
                .status("ACTIVE")
                .amount(new BigDecimal("5000.00"))
                .balance(balance)
                .currency("PEN")
                .termMonths(12)
                .dueDate(dueDate)
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }

    @Test
    void personalSecondCreditIsRejected409() {
        Mockito.doReturn(Mono.just(doc("PERSONAL", new BigDecimal("100.00"), null)))
                .when(repository).findByCustomerIdAndType(customerId, "PERSONAL");

        StepVerifier.create(service.create(customerId, "PERSONAL",
                        new BigDecimal("1000.00"), "PEN", 6, "corr-1"))
                .expectError(CreditConflictException.class)
                .verify();
    }

    @Test
    void businessSecondCreditIsAllowed() {
        Mockito.doReturn(Mono.just(doc("BUSINESS", new BigDecimal("100.00"), null)))
                .when(repository).findByCustomerIdAndType(customerId, "BUSINESS");
        when(repository.save(any(CreditDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.create(customerId, "BUSINESS",
                        new BigDecimal("1000.00"), "PEN", 6, "corr-1"))
                .expectNextMatches(CreditResponse.class::isInstance)
                .verifyComplete();
    }

    @Test
    void patchPaidWithPositiveBalanceIsRejected409() {
        var existing = doc("PERSONAL", new BigDecimal("500.00"), null);
        when(repository.findById(existing.getId())).thenReturn(Mono.just(existing));

        var req = new CreditUpdateRequest();
        req.setStatus(CreditStatus.PAID);

        StepVerifier.create(service.update(existing.getId(), req, "corr"))
                .expectError(CreditConflictException.class)
                .verify();
    }

    @Test
    void patchPaidWithZeroBalanceIsAllowed() {
        var existing = doc("PERSONAL", BigDecimal.ZERO, null);
        when(repository.findById(existing.getId())).thenReturn(Mono.just(existing));
        when(repository.save(any(CreditDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        var req = new CreditUpdateRequest();
        req.setStatus(CreditStatus.PAID);

        StepVerifier.create(service.update(existing.getId(), req, "corr"))
                .expectNextMatches(r -> "PAID".equals(r.getStatus().toString()))
                .verifyComplete();
    }

    @Test
    void findByIdMissingFails404() {
        when(repository.findById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.findById(UUID.randomUUID()))
                .expectError(CreditNotFoundException.class)
                .verify();
    }

    @Test
    void overdueStatusWithOverdueDebt() {
        var existing = doc("PERSONAL", new BigDecimal("300.00"), LocalDate.now().minusDays(10));
        when(repository.findById(existing.getId())).thenReturn(Mono.just(existing));

        StepVerifier.create(service.getOverdueInfo(existing.getId()))
                .expectNextMatches(info -> info.hasOverdueDebt()
                        && info.daysOverdue() == 10
                        && info.overdueAmount().compareTo(new BigDecimal("300.00")) == 0)
                .verifyComplete();
    }

    @Test
    void overdueStatusWithoutDebt() {
        var existing = doc("PERSONAL", BigDecimal.ZERO, LocalDate.now().minusDays(10));
        when(repository.findById(existing.getId())).thenReturn(Mono.just(existing));

        StepVerifier.create(service.getOverdueInfo(existing.getId()))
                .expectNextMatches(info -> !info.hasOverdueDebt() && info.daysOverdue() == 0)
                .verifyComplete();
    }

    @Test
    void deactivateMarksCancelled() {
        var existing = doc("PERSONAL", new BigDecimal("50.00"), null);
        when(repository.findById(existing.getId())).thenReturn(Mono.just(existing));
        when(repository.save(any(CreditDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.deactivate(existing.getId(), "corr"))
                .verifyComplete();

        Mockito.verify(repository).save(any(CreditDocument.class));
    }
}
