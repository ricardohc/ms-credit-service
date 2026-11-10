package com.banking.credit.repository;

import com.banking.credit.document.CreditDocument;
import java.util.UUID;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CreditRepository extends ReactiveMongoRepository<CreditDocument, UUID> {

    Mono<Boolean> existsByCustomerIdAndType(UUID customerId, String type);
}