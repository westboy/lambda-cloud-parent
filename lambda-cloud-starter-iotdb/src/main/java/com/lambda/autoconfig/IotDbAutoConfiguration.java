package com.lambda.autoconfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.iotdb.isession.pool.ITableSessionPool;
import org.apache.iotdb.rpc.IoTDBConnectionException;
import org.apache.iotdb.session.pool.SessionPool;
import org.apache.iotdb.session.pool.TableSessionPoolBuilder;
import org.apache.iotdb.session.subscription.ISubscriptionTableSession;
import org.apache.iotdb.session.subscription.ISubscriptionTreeSession;
import org.apache.iotdb.session.subscription.SubscriptionTableSessionBuilder;
import org.apache.iotdb.session.subscription.SubscriptionTreeSessionBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IotDbAutoConfiguration
 *
 * @author Jin
 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP"},
        justification = "springboot properties")
@Slf4j
@RequiredArgsConstructor
@AutoConfiguration
@EnableConfigurationProperties(value = {IotDbProperties.class})
public class IotDbAutoConfiguration {
    private final IotDbProperties iotDbProperties;

    @Bean
    @ConditionalOnProperty(
            prefix = "lambda.iotdb",
            name = {"tree-dialect"},
            havingValue = "true")
    @ConditionalOnMissingBean(SessionPool.class)
    public SessionPool sessionPool() {
        return new SessionPool.Builder()
                .nodeUrls(iotDbProperties.getNodeUrls())
                .user(iotDbProperties.getUser())
                .password(iotDbProperties.getPassword())
                .maxSize(iotDbProperties.getMaxSize())
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "lambda.iotdb",
            name = {"table-dialect"},
            havingValue = "true")
    @ConditionalOnMissingBean(ITableSessionPool.class)
    public ITableSessionPool tableSessionPool() {
        return new TableSessionPoolBuilder()
                .nodeUrls(iotDbProperties.getNodeUrls())
                .user(iotDbProperties.getUser())
                .password(iotDbProperties.getPassword())
                .maxSize(iotDbProperties.getMaxSize())
                .database(iotDbProperties.getDatabase())
                .thriftMaxFrameSize(iotDbProperties.getThriftMaxFrameSize())
                .build();
    }

    @Configuration
    @ConditionalOnProperty(
            prefix = "lambda.iotdb",
            name = {"enable-subscription"},
            havingValue = "true")
    public class IotDbSubscriptionConfiguration {

        @Bean(initMethod = "open", destroyMethod = "close")
        @ConditionalOnProperty(
                prefix = "lambda.iotdb",
                name = {"tree-dialect"},
                havingValue = "true")
        public ISubscriptionTreeSession subscriptionTreeSession() {
            return new SubscriptionTreeSessionBuilder()
                    .host(iotDbProperties.getHost())
                    .port(iotDbProperties.getPort())
                    .username(iotDbProperties.getUser())
                    .password(iotDbProperties.getPassword())
                    .thriftMaxFrameSize(iotDbProperties.getThriftMaxFrameSize())
                    .build();
        }

        @Bean(destroyMethod = "close")
        @ConditionalOnProperty(
                prefix = "lambda.iotdb",
                name = {"table-dialect"},
                havingValue = "true")
        public ISubscriptionTableSession subscriptionTableSession() throws IoTDBConnectionException {
            return new SubscriptionTableSessionBuilder()
                    .host(iotDbProperties.getHost())
                    .port(iotDbProperties.getPort())
                    .username(iotDbProperties.getUser())
                    .password(iotDbProperties.getPassword())
                    .thriftMaxFrameSize(iotDbProperties.getThriftMaxFrameSize())
                    .build();
        }
    }
}
