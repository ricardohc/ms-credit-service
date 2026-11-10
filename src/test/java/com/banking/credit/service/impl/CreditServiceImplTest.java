package com.banking.credit.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.dto.CreditResponse;
import com.banking.credit.dto.CreditStatus;
import com.banking.credit.dto.CreditUpdateRequest;
import com.banking.credit.event.CreditEvent;
import com.banking.credit.event.CreditEventPublisher;
import com.banking.credit.event.factory.impl.CreditEventFactoryImpl;
import com.banking.credit.exception.CreditConflictException;
import com.banking.credit.exception.CreditNotFoundException;
import com.banking.credit.mapper.CreditMapper;
import com.banking.credit.repository.CreditRepository;
import com.banking.credit.service.validator.impl.CreditCreationValidatorImpl;
import com.banking.credit.service.validator.impl.CreditUpdateValidatorImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
        service = new CreditServiceImpl(
                repository,
                new CreditMapper(),
                eventPublisher,
                new CreditEventFactoryImpl(),
                new CreditCreationValidatorImpl(),
                new CreditUpdateValidatorImpl()
        );
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
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void personalSecondCreditIsRejected409() {
        Mockito.doReturn(Mono.just(true))
                .when(repository).existsByCustomerIdAndType(customerId, "PERSONAL");

        StepVerifier.create(service.create(customerId, "PERSONAL",
                        new BigDecimal("1000.00"), "PEN", 6, "corr-1"))
                .expectError(CreditConflictException.class)
                .verify();

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void businessMultipleCreditsAreAllowed() {
        Mockito.doReturn(Mono.just(true))
                .when(repository).existsByCustomerIdAndType(customerId, "BUSINESS");

        when(repository.save(any(CreditDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.create(customerId, "BUSINESS",
                        new BigDecimal("1000.00"), "PEN", 6, "corr-1"))
                .expectNextMatches(CreditResponse.class::isInstance)
                .verifyComplete();
    }

    @Test
    void invalidTypeIsRejected() {
        Mockito.doReturn(Mono.just(false))
                .when(repository).existsByCustomerIdAndType(any(), anyString());

        StepVerifier.create(service.create(customerId, "MORTGAGE",
                        new BigDecimal("1000.00"), "PEN", 6, "corr"))
                .expectErrorMatches(e -> e instanceof CreditConflictException
                        && e.getMessage().contains("Invalid credit type"))
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void createInitializesFieldsCorrectly() {
        Mockito.doReturn(Mono.just(false))
                .when(repository).existsByCustomerIdAndType(customerId, "PERSONAL");
        when(repository.save(any(CreditDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.create(customerId, "PERSONAL",
                        new BigDecimal("2500.50"), "USD", 24, "corr"))
                .expectNextMatches(r ->
                        r.getId() != null
                                && customerId.equals(r.getCustomerId())
                                && "PERSONAL".equals(r.getType().getValue())
                                && "ACTIVE".equals(r.getStatus().getValue())
                                && "2500.50".equals(r.getAmount())
                                && "2500.50".equals(r.getBalance())
                                && "USD".equals(r.getCurrency())
                                && r.getDueDate() != null
                                && r.getCreatedAt() != null)
                .verifyComplete();
    }

    @Test
    void createPublishesInsertEventWithCorrectPayload() {
        Mockito.doReturn(Mono.just(false))
                .when(repository).existsByCustomerIdAndType(customerId, "BUSINESS");
        when(repository.save(any(CreditDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.create(customerId, "BUSINESS",
                        new BigDecimal("999.99"), "PEN", 12, "corr-xyz"))
                .expectNextCount(1)
                .verifyComplete();

        ArgumentCaptor<CreditEvent> captor = ArgumentCaptor.forClass(CreditEvent.class);
        verify(eventPublisher, times(1)).publish(captor.capture());
        CreditEvent event = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(CreditEvent.INSERT, event.getEventType());
        org.junit.jupiter.api.Assertions.assertEquals("corr-xyz", event.getCorrelationId());
        org.junit.jupiter.api.Assertions.assertNotNull(event.getEventId());
        org.junit.jupiter.api.Assertions.assertNotNull(event.getOccurredAt());
        org.junit.jupiter.api.Assertions.assertEquals("999.99", event.getPayload().getAmount());
        org.junit.jupiter.api.Assertions.assertEquals("PEN", event.getPayload().getCurrency());
        org.junit.jupiter.api.Assertions.assertEquals(12, event.getPayload().getTermMonths());
    }

    @Test
    void createPersistsEvenIfEventPublishFails() {
        Mockito.doReturn(Mono.just(false))
                .when(repository).existsByCustomerIdAndType(customerId, "PERSONAL");
        when(repository.save(any(CreditDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(eventPublisher.publish(any())).thenReturn(Mono.error(new RuntimeException("kafka down")));

        StepVerifier.create(service.create(customerId, "PERSONAL",
                        new BigDecimal("100.00"), "PEN", 3, "corr"))
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

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
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
                .expectNextMatches(r -> "PAID".equals(r.getStatus()))
                .verifyComplete();
    }

    @Test
    void patchCancelledIsAllowedRegardlessOfBalance() {
        var existing = doc("BUSINESS", new BigDecimal("1000.00"), null);
        when(repository.findById(existing.getId())).thenReturn(Mono.just(existing));
        when(repository.save(any(CreditDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        var req = new CreditUpdateRequest();
        req.setStatus(CreditStatus.CANCELLED);

        StepVerifier.create(service.update(existing.getId(), req, "corr"))
                .expectNextMatches(r -> "CANCELLED".equals(r.getStatus()))
                .verifyComplete();
    }

    @Test
    void patchOverdueIsAllowed() {
        var existing = doc("PERSONAL", new BigDecimal("300.00"), LocalDate.now().minusDays(5));
        when(repository.findById(existing.getId())).thenReturn(Mono.just(existing));
        when(repository.save(any(CreditDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        var req = new CreditUpdateRequest();
        req.setStatus(CreditStatus.OVERDUE);

        StepVerifier.create(service.update(existing.getId(), req, "corr"))
                .expectNextMatches(r -> "OVERDUE".equals(r.getStatus()))
                .verifyComplete();
    }

    @Test
    void patchWithoutStatusReturnsDocumentUnchanged() {
        var existing = doc("PERSONAL", new BigDecimal("100.00"), null);
        when(repository.findById(existing.getId())).thenReturn(Mono.just(existing));

        var req = new CreditUpdateRequest();

        StepVerifier.create(service.update(existing.getId(), req, "corr"))
                .expectNextMatches(r -> "ACTIVE".equals(r.getStatus()))
                .verifyComplete();

        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void patchUpdatesTimestampAndPublishesUpdateEvent() {
        var existing = doc("PERSONAL", BigDecimal.ZERO, null);
        existing.setUpdatedAt(LocalDateTime.now().minusDays(1));
        var previousUpdatedAt = existing.getUpdatedAt();
        when(repository.findById(existing.getId())).thenReturn(Mono.just(existing));
        when(repository.save(any(CreditDocument.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        var req = new CreditUpdateRequest();
        req.setStatus(CreditStatus.PAID);

        StepVerifier.create(service.update(existing.getId(), req, "corr-upd"))
                .expectNextCount(1)
                .verifyComplete();

        ArgumentCaptor<CreditEvent> captor = ArgumentCaptor.forClass(CreditEvent.class);
        verify(eventPublisher).publish(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(CreditEvent.UPDATE, captor.getValue().getEventType());
        org.junit.jupiter.api.Assertions.assertEquals("corr-upd", captor.getValue().getCorrelationId());
        org.junit.jupiter.api.Assertions.assertTrue(existing.getUpdatedAt().isAfter(previousUpdatedAt));
    }

    @Test
    void findByIdMissingFails404() {
        when(repository.findById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.findById(UUID.randomUUID()))
                .expectError(CreditNotFoundException.class)
                .verify();
    }

    @Test
    void findByIdReturnsMappedDocument() {
        var existing = doc("PERSONAL", new BigDecimal("100.00"), LocalDate.now());
        when(repository.findById(existing.getId())).thenReturn(Mono.just(existing));

        StepVerifier.create(service.findById(existing.getId()))
                .expectNextMatches(d -> d.getId().equals(existing.getId()))
                .verifyComplete();
    }

    @Test
    void findAllAppliesPagination() {
        var d1 = doc("PERSONAL", BigDecimal.TEN, null);
        var d2 = doc("BUSINESS", BigDecimal.ONE, null);
        var d3 = doc("BUSINESS", BigDecimal.ONE, null);
        when(repository.findAll()).thenReturn(Flux.just(d1, d2, d3));

        StepVerifier.create(service.findAll(1, 2))
                .expectNext(d3)
                .verifyComplete();
    }

    @Test
    void findAllReturnsEmptyWhenPageOutOfRange() {
        var d1 = doc("PERSONAL", BigDecimal.TEN, null);
        when(repository.findAll()).thenReturn(Flux.just(d1));

        StepVerifier.create(service.findAll(5, 10))
                .verifyComplete();
    }

    @Test
    void findAllReturnsAllWhenSizeGreaterThanTotal() {
        var d1 = doc("PERSONAL", BigDecimal.TEN, null);
        var d2 = doc("BUSINESS", BigDecimal.ONE, null);
        when(repository.findAll()).thenReturn(Flux.just(d1, d2));

        StepVerifier.create(service.findAll(0, 100))
                .expectNextCount(2)
                .verifyComplete();
    }
}