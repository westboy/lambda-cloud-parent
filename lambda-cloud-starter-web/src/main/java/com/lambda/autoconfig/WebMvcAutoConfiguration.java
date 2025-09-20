package com.lambda.autoconfig;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lambda.cloud.core.jackson.JacksonModuleConfigurer;
import com.lambda.cloud.core.jackson.LambdaObjectMapper;
import com.lambda.cloud.core.jackson.text.ExtendDateFormat;
import com.lambda.cloud.core.shared.CorsProperty;
import com.lambda.cloud.mvc.StringToDateConverter;
import com.lambda.cloud.mvc.execption.GlobalControllerAdvice;
import com.lambda.cloud.mvc.filter.OrderedTimeHandlerFilter;
import com.lambda.cloud.mvc.filter.XframeOptionsFilter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
    @SuppressWarnings("all")
    public WebMvcConfigurer webMvcConfigurer(
            CorsProperty corsProperty, LocalValidatorFactoryBean defaultValidator) {
        return new WebMvcConfigurer() {
            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(new StringToDateConverter());
            }

            @Override
            public Validator getValidator() {
                return defaultValidator;
            }

            @Override
            public void addCorsMappings(CorsRegistry registry) {
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
    @Bean("jacksonObjectMapper")
    @ConditionalOnMissingBean(name = "jacksonObjectMapper")
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.createXmlMapper(false)
                .serializationInclusion(Include.NON_NULL)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public LambdaObjectMapper objectMapper() {
        return new LambdaObjectMapper();
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
            Jackson2ObjectMapperBuilder builder, List<Module> modules) {
        ObjectMapper mapper = builder.dateFormat(new ExtendDateFormat())
                .featuresToDisable(SerializationFeature.INDENT_OUTPUT)
                .serializationInclusion(Include.NON_NULL)
                .featuresToEnable(Feature.ALLOW_UNQUOTED_FIELD_NAMES, MapperFeature.PROPAGATE_TRANSIENT_MARKER)
                .modules(modules)
                .build();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        converter.setSupportedMediaTypes(supportedMediaTypes);
        return converter;
    }

    @Bean
    public HttpMessageConverters httpMessageConverters(
            MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter,
            StringHttpMessageConverter stringHttpMessageConverter) {
        return new HttpMessageConverters(mappingJackson2HttpMessageConverter, stringHttpMessageConverter);
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
            if (properties.getEncoding() != null) {
                resolver.setCharacterEncoding(properties.getEncoding().name());
            }
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
    @SuppressWarnings("all")
    public FilterRegistrationBean<XframeOptionsFilter> xframeOptionsFilter() {
        FilterRegistrationBean<XframeOptionsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XframeOptionsFilter());
        registrationBean.addUrlPatterns("*.html");
        return registrationBean;
    }
}
