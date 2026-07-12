package com.lambda.autoconfig;

import static java.util.concurrent.TimeUnit.MINUTES;

import com.lambda.cloud.feign.codec.CustomErrorDecoder;
import com.lambda.cloud.feign.interceptors.AuthorizationRequestHeaderInterceptor;
import com.lambda.cloud.feign.interceptors.SaSameTokenRequestInterceptor;
import com.lambda.cloud.feign.webflux.AttributeHolder;
import feign.Contract;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author westboy
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableFeignClients(
        basePackages =
                "${spring.cloud.openfeign.client.base-package:${spring.cloud.openfeign.client.basePackage:com.lambda.cloud}}")
@EnableConfigurationProperties(ExtendFeignClientProperties.class)
public class FeignAutoConfiguration {

    @Bean
    @Primary
    public ExtendFeignClientProperties customizeFeignClientProperties() {
        return new ExtendFeignClientProperties();
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.cloud.openfeign.client.retry", name = "enabled", matchIfMissing = true)
    public Retryer retryer(ExtendFeignClientProperties properties) {
        int maxAttempts = properties.getRetry().getMaxAttempts();
        return new Retryer.Default(100, MINUTES.toMillis(1), maxAttempts);
    }

    @Bean
    public AttributeHolder attributeHolder() {
        return new AttributeHolder();
    }

    @Bean
    @ConditionalOnMissingBean(ErrorDecoder.class)
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    @Bean
    public Contract feignContract() {
        return new SpringMvcContract();
    }

    @Bean
    public RequestInterceptor defaultHeaderInterceptor() {
        log.trace("default request header interceptor is initializing...");
        return new AuthorizationRequestHeaderInterceptor();
    }

    @Bean
    public RequestInterceptor saSameTokenRequestInterceptor() {
        log.trace("sa-token same-token request interceptor is initializing...");
        return new SaSameTokenRequestInterceptor();
    }
}
