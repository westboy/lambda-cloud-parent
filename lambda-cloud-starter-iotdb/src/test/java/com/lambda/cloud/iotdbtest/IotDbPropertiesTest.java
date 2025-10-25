package com.lambda.cloud.iotdbtest;

import static org.assertj.core.api.Assertions.assertThat;

import com.lambda.autoconfig.IotDbAutoConfiguration;
import com.lambda.autoconfig.IotDbProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class IotDbPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(IotDbAutoConfiguration.class));

    @Test
    void bindsBasicValues() {
        contextRunner
                .withPropertyValues(
                        "lambda.iotdb.host=127.0.0.1",
                        "lambda.iotdb.port=6667",
                        "lambda.iotdb.user=root",
                        "lambda.iotdb.password=root",
                        "lambda.iotdb.database=root.lambda.test",
                        "lambda.iotdb.timeout=30000",
                        "lambda.iotdb.max-size=20",
                        "lambda.iotdb.ttl=7200",
                        "lambda.iotdb.thrift-max-frame-size=134217728",
                        "lambda.iotdb.base-package=com.test.package")
                .run(context -> {
                    IotDbProperties p = context.getBean(IotDbProperties.class);
                    assertThat(p.getHost()).isEqualTo("127.0.0.1");
                    assertThat(p.getPort()).isEqualTo(6667);
                    assertThat(p.getUser()).isEqualTo("root");
                    assertThat(p.getPassword()).isEqualTo("root");
                    assertThat(p.getDatabase()).isEqualTo("root.lambda.test");
                    assertThat(p.getTimeout()).isEqualTo(30000L);
                    assertThat(p.getMaxSize()).isEqualTo(20);
                    assertThat(p.getTtl()).isEqualTo(7200L);
                    assertThat(p.getThriftMaxFrameSize()).isEqualTo(134217728);
                    assertThat(p.getBasePackage()).isEqualTo("com.test.package");
                });
    }

    @Test
    void nodeUrlsConfigAndFallback() {
        contextRunner
                .withPropertyValues("lambda.iotdb.node-urls=node1:6667,node2:6667,node3:6667")
                .run(context -> {
                    IotDbProperties p = context.getBean(IotDbProperties.class);
                    assertThat(p.getNodeUrls()).containsExactlyInAnyOrder("node1:6667", "node2:6667", "node3:6667");
                });

        contextRunner
                .withPropertyValues("lambda.iotdb.host=fallback-host", "lambda.iotdb.port=8888")
                .run(context -> {
                    IotDbProperties p = context.getBean(IotDbProperties.class);
                    assertThat(p.getNodeUrls()).containsExactly("fallback-host:8888");
                });
    }
}
