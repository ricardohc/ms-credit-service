package com.banking.credit.repository;

import com.banking.credit.document.CreditDocument;
import java.util.UUID;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Repositorio de escritura directa a MongoDB (banking_credit.credits).
 */
@Repository
public interface CreditRepository extends ReactiveMongoRepository<CreditDocument, UUID> {

    // Devuelve true si existe al menos un crédito con ese cliente y tipo.
    // Es la forma más segura y eficiente de validar sin traer múltiples documentos.
    Mono<Boolean> existsByCustomerIdAndType(UUID customerId, String type);
}