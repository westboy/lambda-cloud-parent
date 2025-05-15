package com.lambda.autoconfig;

import com.lambda.cloud.actuator.MeterHelper;
import com.lambda.cloud.actuator.resolver.PathResourceResolver;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jin
 */
@AutoConfigureAfter({MetricsAutoConfiguration.class, PrometheusMetricsExportAutoConfiguration.class})
@Configuration(proxyBeanMethods = false)
public class ActuatorAutoConfiguration {


    @Bean
    protected MeterRegistryCustomizer<MeterRegistry> customize(
            @Value("${spring.application.name}") String applicationName) {
        return registry -> registry.config().commonTags("application", applicationName);
    }

    @Bean
    public CountedAspect countedAspect(MeterRegistry meterRegistry) {
        return new CountedAspect(meterRegistry);
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }

    @Bean
    public MeterHelper meterHelper(MeterRegistry meterRegistry) {
        return new MeterHelper(meterRegistry);
    }
    @Bean
    public PathResourceResolver pathResourceResolver() {
        return new PathResourceResolver();
    }

}
