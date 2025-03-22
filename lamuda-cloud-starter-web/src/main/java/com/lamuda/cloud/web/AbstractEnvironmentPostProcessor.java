package com.lamuda.cloud.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.NonNull;

/**
 * @author w
 */
public abstract class AbstractEnvironmentPostProcessor implements EnvironmentPostProcessor, ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    protected static DeferredLog log = new DeferredLog();

    /**
     * postProcessEnvironment
     *
     * @param environment
     * @param application
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
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
        if (event.getSource() instanceof SpringApplication) {
            SpringApplication application = (SpringApplication) event.getSource();
            if (WebApplicationType.SERVLET.equals(application.getWebApplicationType())) {
                log.replayTo(application.getMainApplicationClass());
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
