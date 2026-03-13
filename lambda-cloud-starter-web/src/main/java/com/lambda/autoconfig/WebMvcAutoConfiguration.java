package com.lambda.autoconfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lambda.cloud.core.jackson.JacksonModuleConfigurer;
import com.lambda.cloud.core.jackson.text.ExtendDateFormat;
import com.lambda.cloud.core.shared.CorsProperty;
import com.lambda.cloud.mvc.StringToDateConverter;
import com.lambda.cloud.mvc.execption.GlobalControllerAdvice;
import com.lambda.cloud.mvc.filter.OrderedTimeHandlerFilter;
import com.lambda.cloud.mvc.filter.XframeOptionsFilter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.format.FormatterRegistry;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * WebMvcAutoConfiguration
 *
 * @author Jin
 */
@Slf4j
@Import(JacksonModuleConfigurer.class)
@AutoConfiguration
public class WebMvcAutoConfiguration {
    public WebMvcAutoConfiguration() {
        log.trace("initializing...");
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(CorsProperty corsProperty, LocalValidatorFactoryBean defaultValidator) {
        return new WebMvcConfigurer() {
            @Override
            public void addFormatters(@NonNull FormatterRegistry registry) {
                registry.addConverter(new StringToDateConverter());
            }

            @Override
            public Validator getValidator() {
                return defaultValidator;
            }

            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                if (corsProperty.isEnabled()) {
                    CorsRegistration registration = registry.addMapping(CorsProperty.ALL_PATH);
                    List<String> allowedOrigins = corsProperty.getAllowedOrigins();
                    if (CollectionUtils.isNotEmpty(allowedOrigins)) {
                        registration.allowedOriginPatterns(allowedOrigins.toArray(new String[0]));
                    } else {
                        registration.allowedOriginPatterns(CorsProperty.ALL);
                    }
                    registration
                            .allowCredentials(true)
                            .allowedMethods(CorsProperty.ALLOWED_METHOD.toArray(new String[0]))
                            .exposedHeaders(CorsProperty.EXPOSED_HEADERS.toArray(new String[0]))
                            .allowedHeaders(CorsProperty.ALLOWED_HEADERS.toArray(new String[0]))
                            .maxAge(corsProperty.getMaxAge());
                }
            }
        };
    }

    @Bean
    @ConfigurationProperties(prefix = "lambda.web.cors")
    public CorsProperty corsProperties() {
        return new CorsProperty();
    }

    @Primary
    @Bean("jacksonJsonMapper")
    public ObjectMapper jacksonJsonMapper(List<JacksonModule> customModules) {
        JsonMapper.Builder builder = JsonMapper.builder()
                .changeDefaultPropertyInclusion(inc -> inc.withValueInclusion(JsonInclude.Include.NON_NULL))
                .defaultDateFormat(new ExtendDateFormat())
                .enable(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES)
                .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                .disable(SerializationFeature.INDENT_OUTPUT);

        if (customModules != null && !customModules.isEmpty()) {
            builder.addModules(customModules);
        }
        return builder.build();
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return localeResolver;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(SpringTemplateEngine.class)
    public static class ThymeleafAutoConfiguration {

        @Primary
        @Bean
        public SpringResourceTemplateResolver defaultTemplateResolver(
                ApplicationContext applicationContext, ThymeleafProperties properties) {
            SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
            resolver.setApplicationContext(applicationContext);
            resolver.setPrefix(properties.getPrefix());
            resolver.setSuffix(properties.getSuffix());
            resolver.setTemplateMode("HTML");
            resolver.setCharacterEncoding(properties.getEncoding().name());
            resolver.setCacheable(properties.isCache());
            Integer order = properties.getTemplateResolverOrder();
            if (order != null) {
                resolver.setOrder(order);
            }

            Method setCheckExistence =
                    ReflectionUtils.findMethod(resolver.getClass(), "setCheckExistence", boolean.class);
            if (setCheckExistence != null) {
                ReflectionUtils.invokeMethod(setCheckExistence, resolver, properties.isCheckTemplate());
            }
            return resolver;
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public OrderedTimeHandlerFilter timeHandlerFilter() {
        return new OrderedTimeHandlerFilter();
    }

    @Bean
    public GlobalControllerAdvice globalControllerAdvice() {
        return new GlobalControllerAdvice();
    }

    @Bean
    public FilterRegistrationBean<XframeOptionsFilter> xframeOptionsFilter() {
        FilterRegistrationBean<XframeOptionsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XframeOptionsFilter());
        registrationBean.addUrlPatterns("*.html");
        return registrationBean;
    }
}
