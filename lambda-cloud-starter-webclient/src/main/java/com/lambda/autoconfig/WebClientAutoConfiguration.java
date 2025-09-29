package com.lambda.autoconfig;

import com.lambda.cloud.webclient.WebClientTemplate;
import com.lambda.cloud.webclient.WebClientTemplateFactory;
import com.lambda.cloud.webclient.authorization.AuthorizationExchangeFilterFunction;
import com.lambda.cloud.webclient.hmac.HmacExchangeFilterFunction;
import com.lambda.cloud.webclient.logging.LoggingExchangeFilterFunction;
import com.lambda.cloud.webclient.metrics.MetricsExchangeFilterFunction;
import com.lambda.cloud.webclient.retry.RetryExchangeFilterFunction;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient自动配置
 *
 * @author jpjoo
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(WebClientProperties.class)
@ConditionalOnProperty(prefix = "lambda.webclient", name = "enabled", matchIfMissing = true)
@ConditionalOnClass(WebClient.class)
public class WebClientAutoConfiguration {

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public LoggingExchangeFilterFunction loggingExchangeFilterFunction() {
        return new LoggingExchangeFilterFunction();
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    @ConditionalOnClass(MeterRegistry.class)
    public MetricsExchangeFilterFunction metricsExchangeFilterFunction(
            @SuppressWarnings("all") MeterRegistry meterRegistry) {
        return new MetricsExchangeFilterFunction(meterRegistry);
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public HmacExchangeFilterFunction hmacExchangeFilterFunction() {
        return new HmacExchangeFilterFunction();
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public RetryExchangeFilterFunction retryExchangeFilterFunction() {
        return new RetryExchangeFilterFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationExchangeFilterFunction authorizationExchangeFilterFunction() {
        return new AuthorizationExchangeFilterFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClientTemplateFactory webClientFactory(WebClientProperties properties) {
        return new WebClientTemplateFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClientTemplate webClientService(WebClientTemplateFactory webClientTemplateFactory) {
        return new WebClientTemplate(webClientTemplateFactory);
    }

    @Bean("defaultWebClient")
    @ConditionalOnMissingBean(name = "defaultWebClient")
    public WebClient defaultWebClient(WebClientTemplateFactory webClientTemplateFactory) {
        return webClientTemplateFactory.createDefault();
    }
}
