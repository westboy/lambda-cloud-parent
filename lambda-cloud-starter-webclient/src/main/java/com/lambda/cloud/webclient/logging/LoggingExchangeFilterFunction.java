package com.lambda.cloud.webclient.logging;

import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * 日志过滤器
 *
 * @author jpjoo
 */
@Slf4j
public class LoggingExchangeFilterFunction implements ExchangeFilterFunction {
    private String clientName = "default";

    public LoggingExchangeFilterFunction withName(String name) {
        this.clientName = name;
        return this;
    }

    @NonNull
    @Override
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {
        Instant start = Instant.now();

        if (log.isDebugEnabled()) {
            log.debug("WebClient [ {} ] Request: {} {}", clientName, request.method(), request.url());
            request.headers()
                    .forEach((name, values) ->
                            log.debug("WebClient [ {} ] Request Header: {}={}", clientName, name, values));
        }

        return next.exchange(request)
                .doOnNext(response -> {
                    Duration duration = Duration.between(start, Instant.now());
                    if (log.isDebugEnabled()) {
                        log.debug(
                                "WebClient  [ {} ]  Response: {} {} - {} ({}ms)",
                                clientName,
                                request.method(),
                                request.url(),
                                response.statusCode(),
                                duration.toMillis());
                        response.headers()
                                .asHttpHeaders()
                                .forEach((name, values) -> log.debug(
                                        "WebClient  [ {} ]  Response  Header: {}={}", clientName, name, values));
                    }
                })
                .doOnError(error -> {
                    Duration duration = Duration.between(start, Instant.now());
                    log.error(
                            "WebClient  [ {} ]  Error: {} {} - {} ({}ms)",
                            clientName,
                            request.method(),
                            request.url(),
                            error.getMessage(),
                            duration.toMillis());
                });
    }
}
