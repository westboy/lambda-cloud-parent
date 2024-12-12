package com.jingfang.autoconfig;

import com.jingfang.cloud.feign.codec.CustomErrorDecoder;
import com.jingfang.cloud.feign.interceptors.AuthorizationRequestHeaderInterceptor;
import com.jingfang.cloud.feign.webflux.AttributeHolder;
import feign.Contract;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author westboy
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableFeignClients(basePackages = "${spring.cloud.openfeign.client.basePackage:com.jingfang.cloud}")
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
    public Decoder springDecoder(ObjectFactory<HttpMessageConverters> messageConverters,
                                 ObjectProvider<HttpMessageConverterCustomizer> customizers) {
        return new SpringDecoder(messageConverters, customizers);
    }


    @Bean
    public RequestInterceptor defaultHeaderInterceptor() {
        log.trace("default request header interceptor is initializing...");
        return new AuthorizationRequestHeaderInterceptor();
    }

}
