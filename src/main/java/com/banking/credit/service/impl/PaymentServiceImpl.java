package com.banking.credit.service.impl;

import com.banking.credit.document.CreditDocument;
import com.banking.credit.exception.CreditNotFoundException;
import com.banking.credit.exception.PaymentExceedsBalanceException;
import com.banking.credit.exception.TransactionServiceException;
import com.banking.credit.repository.CreditRepository;
import com.banking.credit.dto.OperationAcceptedResponse;
import com.banking.credit.service.PaymentService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Fachada de pagos: delega la operacion en ms-transaction-service.
 * - Registra idempotencia por X-Transaction-ID (repeticion no genera doble efecto).
 * - Timeout de Resilience4j (2 s) aplicado en la llamada saliente.
 * - El saldo/estado del credito se actualiza por el flujo de eventos/transaction-service,
 *   sin escrituras cruzadas a otras bases.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final CreditRepository creditRepository;
    private final WebClient transactionWebClient;

    @Value("${transaction-service.base-url:http://ms-transaction-service}")
    private String baseUrl;

    /** Registro de idempotencia en memoria por transaccion (transactionId -> respuesta). */
    private final Map<UUID, OperationAcceptedResponse> idempotencyRegistry = new HashMap<>();

    @Override
    public Mono<OperationAcceptedResponse> pay(UUID creditId, UUID transactionId,
                                               BigDecimal amount, String currency,
                                               UUID sourceAccountId, UUID payerCustomerId,
                                               String correlationId) {
        // 1) Idempotencia: mismo X-Transaction-ID retorna la transaccion original.
        synchronized (idempotencyRegistry) {
            var previous = idempotencyRegistry.get(transactionId);
            if (previous != null) {
                log.info("Idempotent replay for transactionId={}", transactionId);
                return Mono.just(previous);
            }
        }

        // 2) Validar credito y deuda pendiente.
        return creditRepository.findById(creditId)
                .switchIfEmpty(Mono.error(new CreditNotFoundException(
                        "Credit not found: " + creditId)))
                .flatMap(credit -> validate(credit, amount, currency))
                .flatMap(credit -> callTransactionService(creditId, transactionId, amount,
                        currency, sourceAccountId, payerCustomerId, correlationId));
    }

    private Mono<CreditDocument> validate(CreditDocument credit, BigDecimal amount, String currency) {
        if (!currency.equals(credit.getCurrency())) {
            return Mono.error(new PaymentExceedsBalanceException(
                    "Payment currency " + currency + " does not match credit currency "
                            + credit.getCurrency()));
        }
        if (amount.compareTo(credit.getBalance()) > 0) {
            return Mono.error(new PaymentExceedsBalanceException(
                    "Payment " + amount + " exceeds outstanding balance "
                            + credit.getBalance()));
        }
        return Mono.just(credit);
    }

    private Mono<OperationAcceptedResponse> callTransactionService(
            UUID creditId, UUID transactionId, BigDecimal amount, String currency,
            UUID sourceAccountId, UUID payerCustomerId, String correlationId) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("creditId", creditId);
        payload.put("transactionId", transactionId);
        payload.put("amount", amount.toPlainString());
        payload.put("currency", currency);
        payload.put("sourceAccountId", sourceAccountId);
        if (payerCustomerId != null) {
            payload.put("payerCustomerId", payerCustomerId);
        }

        return transactionWebClient.post()
                .uri("/api/v1/transactions/credits/payments")
                .header("X-Correlation-ID", correlationId)
                .header("X-Transaction-ID", transactionId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> status.value() >= 400, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new TransactionServiceException(
                                        "Transaction service rejected payment: "
                                                + response.statusCode() + " " + body))))
                .bodyToMono(OperationAcceptedResponse.class)
                .timeout(java.time.Duration.ofSeconds(2))
                .doOnNext(resp -> {
                    synchronized (idempotencyRegistry) {
                        idempotencyRegistry.put(transactionId, resp);
                    }
                    log.info("Payment delegated for credit {} [transactionId={}, status={}]",
                            creditId, transactionId, resp.getStatus());
                })
                .onErrorMap(err -> err instanceof TransactionServiceException ? err
                        : new TransactionServiceException(
                        "Failed to reach transaction service: " + err.getMessage()));
    }
}
