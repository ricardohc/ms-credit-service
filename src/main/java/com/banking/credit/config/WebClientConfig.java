package com.banking.credit.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * WebClient hacia ms-transaction-service con timeout de 2 segundos
 * (regla transversal de Resilience4j / timeouts inter-servicios).
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient transactionWebClient(
            @Value("${transaction-service.base-url:http://ms-transaction-service}") String baseUrl) {
        var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .responseTimeout(Duration.ofSeconds(2))
                .doOnConnected(conn -> conn.addHandlerLast(
                        new ReadTimeoutHandler(2, TimeUnit.SECONDS)));
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
