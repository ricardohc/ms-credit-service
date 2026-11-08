package com.banking.credit.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Productor de eventos de dominio hacia el topico banking.credit.events.v1.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditEventPublisher {

    public static final String TOPIC = "banking.credit.events.v1";

    private final ReactiveKafkaProducerTemplate<String, CreditEvent> kafkaTemplate;

    public Mono<Void> publish(CreditEvent event) {
        return kafkaTemplate.send(TOPIC, event.getCreditId().toString(), event)
                .doOnSuccess(result -> log.info(
                        "Event {} published for credit {} [correlationId={}]",
                        event.getEventType(), event.getCreditId(), event.getCorrelationId()))
                .doOnError(err -> log.error(
                        "Failed to publish event {} for credit {}: {}",
                        event.getEventType(), event.getCreditId(), err.getMessage()))
                .onErrorResume(err -> Mono.empty())
                .then();
    }
}
