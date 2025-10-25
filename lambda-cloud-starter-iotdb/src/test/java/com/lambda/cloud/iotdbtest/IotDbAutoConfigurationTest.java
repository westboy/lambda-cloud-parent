package com.lambda.cloud.iotdbtest;

import static org.assertj.core.api.Assertions.assertThat;

import com.lambda.autoconfig.IotDbAutoConfiguration;
import com.lambda.autoconfig.IotDbProperties;
import com.lambda.cloud.iotdb.manager.IotDbConsumerManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class IotDbAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(IotDbAutoConfiguration.class));

    @Test
    void whenSubscriptionEnabledCreatesManagerBean() {
        contextRunner
                .withPropertyValues(
                        "lambda.iotdb.enable-subscription=true",
                        "lambda.iotdb.host=localhost",
                        "lambda.iotdb.port=6667",
                        "lambda.iotdb.user=root",
                        "lambda.iotdb.password=root")
                .run(context -> {
                    assertThat(context).hasSingleBean(IotDbProperties.class);
                    assertThat(context).hasSingleBean(IotDbConsumerManager.class);
                });
    }

    @Test
    void whenSubscriptionDisabledDoesNotCreateManagerBean() {
        contextRunner
                .withPropertyValues(
                        "lambda.iotdb.enable-subscription=false",
                        "lambda.iotdb.host=localhost",
                        "lambda.iotdb.port=6667")
                .run(context -> {
                    assertThat(context).hasSingleBean(IotDbProperties.class);
                    assertThat(context).doesNotHaveBean(IotDbConsumerManager.class);
                });
    }
}
