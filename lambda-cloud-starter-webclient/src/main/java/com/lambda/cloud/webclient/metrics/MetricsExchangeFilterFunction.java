package com.lambda.cloud.webclient.metrics;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * 指标过滤器
 *
 * @author jpjoo
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
@RequiredArgsConstructor
@ConditionalOnClass(MeterRegistry.class)
public class MetricsExchangeFilterFunction implements ExchangeFilterFunction {

    private final MeterRegistry meterRegistry;
    private String clientName = "default";

    public MetricsExchangeFilterFunction withName(String name) {
        this.clientName = name;
        return this;
    }

    @NonNull
    @Override
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, ExchangeFunction next) {
        Timer.Sample sample = Timer.start(meterRegistry);

        return next.exchange(request)
                .doOnNext(response -> sample.stop(Timer.builder("webclient.requests")
                        .tag("client", clientName)
                        .tag("method", request.method().name())
                        .tag("status", String.valueOf(response.statusCode().value()))
                        .tag("outcome", response.statusCode().is2xxSuccessful() ? "SUCCESS" : "ERROR")
                        .register(meterRegistry)))
                .doOnError(error -> sample.stop(Timer.builder("webclient.requests")
                        .tag("client", clientName)
                        .tag("method", request.method().name())
                        .tag("status", "UNKNOWN")
                        .tag("outcome", "ERROR")
                        .register(meterRegistry)));
    }
}
