package com.lambda.cloud.web;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author w
 */
public abstract class AbstractEnvironmentPostProcessor
        implements EnvironmentPostProcessor, ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    protected static final DeferredLog log = new DeferredLog();

    /**
     * postProcessEnvironment
     *
     * @param environment
     * @param application
     */
    @Override
    public void postProcessEnvironment(
            @org.jspecify.annotations.NonNull ConfigurableEnvironment environment, SpringApplication application) {
        if (WebApplicationType.SERVLET.equals(application.getWebApplicationType())) {
            process(environment, application);
        }
    }

    /**
     * onApplicationEvent
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(@NonNull ApplicationEnvironmentPreparedEvent event) {
        if (event.getSource() instanceof SpringApplication application) {
            if (WebApplicationType.SERVLET.equals(application.getWebApplicationType())) {
                if (application.getMainApplicationClass() != null) {
                    log.replayTo(application.getMainApplicationClass());
                }
            }
        }
    }

    /**
     * process
     * @param environment
     * @param application
     */
    protected abstract void process(ConfigurableEnvironment environment, SpringApplication application);
}
