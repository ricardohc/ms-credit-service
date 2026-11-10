package com.banking.credit.exception;

import com.banking.credit.dto.ErrorResponse;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static ErrorResponse build(String code, String message, String correlationId,
                                       List<String> details) {
        var e = new ErrorResponse();
        e.setCode(code);
        e.setMessage(message);
        e.setTimestamp(OffsetDateTime.now());
        e.setCorrelationId(correlationId);
        e.setDetails(details);
        return e;
    }

    private static String correlation(ServerWebExchange exchange) {
        var v = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        return v == null ? "" : v;
    }

    @ExceptionHandler(CreditNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> notFound(CreditNotFoundException ex,
                                                        ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build(ex.getCode(), ex.getMessage(), correlation(exchange), null)));
    }

    @ExceptionHandler(CreditConflictException.class)
    public Mono<ResponseEntity<ErrorResponse>> conflict(CreditConflictException ex,
                                                        ServerWebExchange exchange) {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(build(ex.getCode(), ex.getMessage(), correlation(exchange), null)));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> badRequest(WebExchangeBindException ex,
                                                          ServerWebExchange exchange) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return Mono.just(ResponseEntity.badRequest()
                .body(build("BAD_REQUEST", "Invalid request", correlation(exchange), details)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> generic(Exception ex, ServerWebExchange exchange) {
        log.error("Unhandled error", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build("INTERNAL_ERROR", "Internal server error", correlation(exchange), null)));
    }
}