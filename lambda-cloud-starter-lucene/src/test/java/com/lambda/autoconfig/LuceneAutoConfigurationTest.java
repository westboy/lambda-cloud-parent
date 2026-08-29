package com.lambda.autoconfig;

import static org.assertj.core.api.Assertions.assertThat;

import com.lambda.cloud.lucene.manager.LuceneManagerFactory;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class LuceneAutoConfigurationTest {

    @TempDir
    Path tempDirectory;

    @Test
    void disabledByDefaultDoesNotCreateLocalInfrastructure() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LuceneAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(LuceneManagerFactory.class));
    }

    @Test
    void enabledCreatesFactoryAtConfiguredDirectory() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LuceneAutoConfiguration.class))
                .withPropertyValues(
                        "lambda.lucene.enabled=true", "lambda.lucene.directory=" + tempDirectory.toAbsolutePath())
                .run(context -> {
                    assertThat(context).hasSingleBean(LuceneManagerFactory.class);
                    assertThat(context.getBean(LuceneManagerFactory.class).getRootDirectory())
                            .isEqualTo(tempDirectory.toAbsolutePath().normalize());
                });
    }
}
