package com.banking.credit.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditEventPublisher {

    public static final String TOPIC = "banking.credit.events.v1";

    private final ReactiveKafkaProducerTemplate<String, CreditEvent> kafkaTemplate;

    public Mono<Void> publish(CreditEvent event) {
        String creditId = event.getPayload().getCreditId().toString();
        return kafkaTemplate.send(TOPIC, creditId, event)
                .doOnSuccess(result -> log.info(
                        "Event {} published for credit {} [correlationId={}]",
                        event.getEventType(), creditId, event.getCorrelationId()))
                .doOnError(err -> log.error(
                        "Failed to publish event {} for credit {}: {}",
                        event.getEventType(), creditId, err.getMessage()))
                .onErrorResume(err -> Mono.empty())
                .then();
    }
}