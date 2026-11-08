package com.banking.credit.service;

import com.banking.credit.dto.OperationAcceptedResponse;
import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * Fachada de pagos: delega en ms-transaction-service (skill-credit.md).
 * Idempotente por X-Transaction-ID.
 */
public interface PaymentService {

    Mono<OperationAcceptedResponse> pay(UUID creditId, UUID transactionId,
                                        java.math.BigDecimal amount, String currency,
                                        UUID sourceAccountId, UUID payerCustomerId,
                                        String correlationId);
}
