package com.jingfang.cloud.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.lang.NonNull;

/**
 * @author Jin
 */
public abstract class JFEnvironmentPostProcessor implements EnvironmentPostProcessor, ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    protected static DeferredLog log = new DeferredLog();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (WebApplicationType.SERVLET.equals(application.getWebApplicationType())) {
            postProcessEnvironment0(environment, application);
        }
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationEnvironmentPreparedEvent event) {
        if (event.getSource() instanceof SpringApplication) {
            SpringApplication application = (SpringApplication) event.getSource();
            if (WebApplicationType.SERVLET.equals(application.getWebApplicationType())) {
                log.replayTo(application.getMainApplicationClass());
            }
        }
    }

    protected abstract void postProcessEnvironment0(ConfigurableEnvironment environment, SpringApplication application);
}
