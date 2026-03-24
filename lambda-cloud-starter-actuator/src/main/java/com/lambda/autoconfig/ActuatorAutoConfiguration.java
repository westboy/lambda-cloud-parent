package com.lambda.autoconfig;

import com.lambda.cloud.actuator.MeterHelper;
import com.lambda.cloud.actuator.resolver.PathResourceResolver;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author jin
 */
@AutoConfigureAfter({MetricsAutoConfiguration.class, PrometheusMetricsExportAutoConfiguration.class})
@AutoConfiguration
@EnableConfigurationProperties({ActuatorProperties.class})
public class ActuatorAutoConfiguration {

    @Bean
    protected MeterRegistryCustomizer<MeterRegistry> customize(
            @Value("${spring.application.name}") String applicationName) {
        return registry -> registry.config().commonTags("application", applicationName);
    }

    @Bean
    public MeterHelper meterHelper(@Lazy MeterRegistry meterRegistry) {
        return new MeterHelper(meterRegistry);
    }

    @Bean
    public PathResourceResolver pathResourceResolver(ActuatorProperties actuatorProperties) {
        ActuatorProperties.Resource resource = actuatorProperties.getResource();
        return new PathResourceResolver(resource.getLocationPattern());
    }

    @Configuration(proxyBeanMethods = false)
    static class MetricsAspectConfiguration {

        @Bean
        public CountedAspect countedAspect(MeterRegistry meterRegistry) {
            return new CountedAspect(meterRegistry);
        }

        @Bean
        public TimedAspect timedAspect(MeterRegistry meterRegistry) {
            return new TimedAspect(meterRegistry);
        }
    }
}
