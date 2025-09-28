package com.lambda.autoconfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.cloud.webclient.WebClientFactory;
import com.lambda.cloud.webclient.WebClientService;
import com.lambda.cloud.webclient.cache.RequestBodyCachingWebFilter;
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
    @ConditionalOnMissingBean
    public LoggingExchangeFilterFunction loggingExchangeFilterFunction() {
        return new LoggingExchangeFilterFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(MeterRegistry.class)
    public MetricsExchangeFilterFunction metricsExchangeFilterFunction(MeterRegistry meterRegistry) {
        return new MetricsExchangeFilterFunction(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public HmacExchangeFilterFunction hmacExchangeFilterFunction() {
        return new HmacExchangeFilterFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryExchangeFilterFunction retryExchangeFilterFunction() {
        return new RetryExchangeFilterFunction();
    }

    /**
     * 请求体缓存WebFilter
     * <p>
     * 当HMAC签名策略配置为INCLUDE_BODY且启用请求体缓存时，
     * 自动注册此WebFilter来缓存请求体内容。
     * </p>
     * 
     * <h3>启用条件：</h3>
     * <ul>
     *   <li>WebClient功能已启用</li>
     *   <li>存在WebClient相关类</li>
     *   <li>未手动配置RequestBodyCachingWebFilter Bean</li>
     * </ul>
     * 
     * <h3>功能特性：</h3>
     * <ul>
     *   <li><strong>条件缓存</strong> - 仅在需要时缓存请求体</li>
     *   <li><strong>大小限制</strong> - 受配置的maxCacheSize限制</li>
     *   <li><strong>自动清理</strong> - 请求完成后自动清理缓存</li>
     *   <li><strong>性能优化</strong> - 避免不必要的缓存操作</li>
     * </ul>
     * 
     * @param properties WebClient配置属性
     * @return RequestBodyCachingWebFilter实例
     */
    @Bean
    @ConditionalOnMissingBean
    public RequestBodyCachingWebFilter requestBodyCachingWebFilter(WebClientProperties properties) {
        return new RequestBodyCachingWebFilter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClientFactory webClientFactory(
            WebClientProperties properties,
            LoggingExchangeFilterFunction loggingFilter,
            MetricsExchangeFilterFunction metricsFilter,
            HmacExchangeFilterFunction hmacFilter,
            RetryExchangeFilterFunction retryFilter) {
        return new WebClientFactory(properties, loggingFilter, metricsFilter, hmacFilter, retryFilter);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebClientService webClientService(WebClientFactory webClientFactory, ObjectMapper objectMapper) {
        return new WebClientService(webClientFactory, objectMapper);
    }

    @Bean("defaultWebClient")
    @ConditionalOnMissingBean(name = "defaultWebClient")
    public WebClient defaultWebClient(WebClientFactory webClientFactory) {
        return webClientFactory.createDefault();
    }
}
