package com.lambda.cloud.webclient.retry;

import com.lambda.autoconfig.WebClientProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * 重试过滤器
 *
 * @author jpjoo
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
@Slf4j
public class RetryExchangeFilterFunction implements ExchangeFilterFunction {

    private WebClientProperties.RetryConfig retryConfig;

    public RetryExchangeFilterFunction withConfig(WebClientProperties.RetryConfig config) {
        this.retryConfig = config;
        return this;
    }

    @NonNull
    @Override
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {
        if (retryConfig == null || !retryConfig.isEnabled()) {
            return next.exchange(request);
        }

        return next.exchange(request)
                .retryWhen(Retry.backoff(retryConfig.getMaxAttempts() - 1, retryConfig.getBackoff())
                        .maxBackoff(retryConfig.getMaxBackoff())
                        .multiplier(retryConfig.getMultiplier())
                        .filter(this::shouldRetry)
                        .doBeforeRetry(retrySignal -> log.warn(
                                "Retrying request {} {} (attempt {})",
                                request.method(),
                                request.url(),
                                retrySignal.totalRetries() + 1)));
    }

    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            return Arrays.stream(retryConfig.getRetryableStatusCodes()).anyMatch(code -> code == statusCode);
        }
        return false;
    }
}
