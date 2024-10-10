package org.apache.dubbo.spring.boot.context.event;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.Nonnull;

/**
 * After LoggingApplicationListener#DEFAULT_ORDER
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20 + 1)
public class WelcomeLogoApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(@Nonnull ApplicationEnvironmentPreparedEvent event) {
        //donothing
    }

}
