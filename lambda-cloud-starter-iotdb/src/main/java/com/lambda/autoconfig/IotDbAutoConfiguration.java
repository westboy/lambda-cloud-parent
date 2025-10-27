package com.lambda.autoconfig;

import com.lambda.cloud.iotdb.IotDbConsumerRegistrar;
import com.lambda.cloud.iotdb.manager.IotDbConsumerManager;
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
@Slf4j
@RequiredArgsConstructor
@AutoConfiguration
@EnableConfigurationProperties(value = {IotDbProperties.class})
public class IotDbAutoConfiguration {
    @Bean
    @ConditionalOnProperty(
            prefix = "lambda.iotdb",
            name = {"tree-dialect"},
            havingValue = "true")
    @ConditionalOnMissingBean(SessionPool.class)
    public SessionPool sessionPool(IotDbProperties iotDbProperties) {
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
    public ITableSessionPool tableSessionPool(IotDbProperties iotDbProperties) {
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
    public static class IotDbSubscriptionConfiguration {

        @Bean(initMethod = "open", destroyMethod = "close")
        @ConditionalOnProperty(
                prefix = "lambda.iotdb",
                name = {"tree-dialect"},
                havingValue = "true")
        public ISubscriptionTreeSession subscriptionTreeSession(IotDbProperties iotDbProperties) {
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
        public ISubscriptionTableSession subscriptionTableSession(IotDbProperties iotDbProperties)
                throws IoTDBConnectionException {
            return new SubscriptionTableSessionBuilder()
                    .host(iotDbProperties.getHost())
                    .port(iotDbProperties.getPort())
                    .username(iotDbProperties.getUser())
                    .password(iotDbProperties.getPassword())
                    .thriftMaxFrameSize(iotDbProperties.getThriftMaxFrameSize())
                    .build();
        }

        @Bean(destroyMethod = "stopAll")
        @ConditionalOnMissingBean(IotDbConsumerManager.class)
        public IotDbConsumerManager iotDbConsumerManager(IotDbProperties iotDbProperties) {
            log.info("Creating IotDbConsumerManager bean");
            return new IotDbConsumerManager(iotDbProperties);
        }

        @Bean
        @ConditionalOnMissingBean(IotDbConsumerRegistrar.class)
        public IotDbConsumerRegistrar iotDbConsumerRegistrar(
                IotDbConsumerManager consumerManager, IotDbProperties iotDbProperties) {
            log.info("Creating IotDbConsumerRegistrar bean with basePackage: {}", iotDbProperties.getBasePackage());
            return new IotDbConsumerRegistrar(consumerManager, iotDbProperties);
        }
    }
}
