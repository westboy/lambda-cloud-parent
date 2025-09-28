package com.lambda.cloud.webclient;

import com.lambda.autoconfig.WebClientProperties;
import com.lambda.cloud.webclient.hmac.HmacExchangeFilterFunction;
import com.lambda.cloud.webclient.logging.LoggingExchangeFilterFunction;
import com.lambda.cloud.webclient.metrics.MetricsExchangeFilterFunction;
import com.lambda.cloud.webclient.retry.RetryExchangeFilterFunction;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * WebClient工厂类
 *
 * @author jpjoo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebClientFactory {

    private final WebClientProperties properties;
    private final LoggingExchangeFilterFunction loggingFilter;
    private final MetricsExchangeFilterFunction metricsFilter;
    private final HmacExchangeFilterFunction hmacFilter;
    private final RetryExchangeFilterFunction retryFilter;

    /**
     * 创建默认WebClient
     */
    public WebClient createDefault() {
        return create("default", properties.getDefaultConfig());
    }

    /**
     * 根据名称创建WebClient
     */
    public WebClient create(String name) {
        WebClientProperties.ClientConfig config = properties.getClients().get(name);
        if (config == null) {
            config = properties.getDefaultConfig();
        }
        return create(name, config);
    }

    /**
     * 创建WebClient
     */
    public WebClient create(String name, WebClientProperties.ClientConfig config) {
        // 创建连接提供者
        ConnectionProvider connectionProvider = createConnectionProvider(name, config);

        // 创建HttpClient
        HttpClient httpClient = createHttpClient(connectionProvider, config);

        // 创建WebClient构建器
        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(config.getMaxInMemorySize()));

        // 设置基础URL
        if (config.getBaseUrl() != null) {
            builder.baseUrl(config.getBaseUrl());
        }

        // 添加默认请求头
        properties.getDefaultHeaders().forEach(builder::defaultHeader);
        config.getHeaders().forEach(builder::defaultHeader);

        // 添加过滤器
        if (properties.isLoggingEnabled()) {
            builder.filter(loggingFilter);
        }

        if (properties.isMetricsEnabled()) {
            builder.filter(metricsFilter.withName(name));
        }

        if (config.getRetry().isEnabled()) {
            builder.filter(retryFilter.withConfig(config.getRetry()));
        }

        if (config.isHmacEnabled()) {
            builder.filter(hmacFilter.withConfig(config.getHmac()));
        }

        return builder.build();
    }

    /**
     * 创建连接提供者
     */
    private ConnectionProvider createConnectionProvider(String name, WebClientProperties.ClientConfig config) {
        WebClientProperties.ConnectionPoolConfig poolConfig = config.getConnectionPool();

        return ConnectionProvider.builder(name)
                .maxConnections(poolConfig.getMaxConnections())
                .maxIdleTime(poolConfig.getMaxIdleTime())
                .maxLifeTime(poolConfig.getMaxLifeTime())
                .pendingAcquireTimeout(poolConfig.getAcquireTimeout())
                .build();
    }

    /**
     * 创建HttpClient
     */
    private HttpClient createHttpClient(
            ConnectionProvider connectionProvider, WebClientProperties.ClientConfig config) {
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int)
                        config.getConnectTimeout().toMillis())
                .responseTimeout(config.getResponseTimeout())
                .doOnConnected(conn -> {
                    conn.addHandlerLast(
                            new ReadTimeoutHandler(config.getReadTimeout().toSeconds(), TimeUnit.SECONDS));
                    conn.addHandlerLast(
                            new WriteTimeoutHandler(config.getWriteTimeout().toSeconds(), TimeUnit.SECONDS));
                });

        // SSL配置
        if (config.getSsl().isEnabled()) {
            httpClient = httpClient.secure(sslSpec -> {
                try {
                    SslContext sslContext = createSslContext(config.getSsl());
                    sslSpec.sslContext(sslContext);
                } catch (SSLException e) {
                    log.error("Failed to create SSL context", e);
                    throw new RuntimeException("Failed to create SSL context", e);
                }
            });
        }

        return httpClient;
    }

    /**
     * 创建SSL上下文
     */
    private SslContext createSslContext(WebClientProperties.SslConfig sslConfig) throws SSLException {
        SslContextBuilder builder = SslContextBuilder.forClient();

        if (sslConfig.isTrustAll()) {
            builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
        }

        // TODO 这里可以添加更多SSL配置，如密钥库、信任库等
        return builder.build();
    }
}
