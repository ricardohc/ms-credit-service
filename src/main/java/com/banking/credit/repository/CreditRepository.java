package com.banking.credit.repository;

import com.banking.credit.document.CreditDocument;
import java.util.UUID;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Escritura directa a MongoDB propia (banking_credit.credits) via ReactiveMongoRepository.
 */
@Repository
public interface CreditRepository extends ReactiveMongoRepository<CreditDocument, UUID> {

    Flux<CreditDocument> findAll();

    Mono<CreditDocument> findByCustomerIdAndType(UUID customerId, String type);
}
