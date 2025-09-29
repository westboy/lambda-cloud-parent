package com.lambda.cloud.webclient.hmac;

import static com.lambda.cloud.core.Constants.GSON;

import com.lambda.autoconfig.WebClientProperties;
import com.lambda.cloud.core.utils.HmacGenerator;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * HMAC认证过滤器
 * <p>
 * 基于Spring WebClient的ExchangeFilterFunction实现HMAC认证。
 * 该过滤器会在请求发送前自动添加HMAC签名认证头。
 * </p>
 *
 * <h3>功能特性：</h3>
 * <ul>
 *   <li>自动生成HMAC签名</li>
 *   <li>支持查询参数签名</li>
 *   <li>支持请求体签名</li>
 *   <li>时间戳防重放</li>
 * </ul>
 *
 * @author jpjoo
 */
@Slf4j
@RequiredArgsConstructor
public class HmacExchangeFilterFunction implements ExchangeFilterFunction {
    private WebClientProperties.HmacConfig hmacConfig;

    /**
     * 配置HMAC参数
     *
     * @param config HMAC配置
     * @return 当前实例
     */
    public HmacExchangeFilterFunction withConfig(WebClientProperties.HmacConfig config) {
        this.hmacConfig = config;
        return this;
    }

    /**
     *
     * @param request the current request
     * @param next the next exchange function in the chain
     * @return Mono<ClientResponse>
     */
    @NonNull
    @Override
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {
        if (!hmacConfig.isEnabled()) {
            log.debug("HMAC配置未启用，跳过HMAC认证");
            return next.exchange(request);
        }
        return extractRequestBodyFromBody(request).flatMap(requestBody -> Mono.fromCallable(() -> {
                    try {
                        // 生成时间戳（毫秒）
                        long timestamp = System.currentTimeMillis();

                        // 提取查询参数
                        Map<String, String[]> queryParams = extractQueryParams(request);

                        // 生成基础签名字符串
                        String baseString =
                                HmacGenerator.baseString(hmacConfig.getAppId(), timestamp, queryParams, requestBody);

                        // 生成Authorization头
                        String authorization = HmacGenerator.authorization(
                                hmacConfig.getAppId(), hmacConfig.getSecret(), timestamp, baseString);

                        // 构建新的请求，添加认证头
                        ClientRequest newRequest = ClientRequest.from(request)
                                .header("Authorization", authorization)
                                .build();

                        log.debug("HMAC认证头已添加: {}", authorization);
                        return newRequest;
                    } catch (UnsupportedEncodingException e) {
                        log.error("HMAC签名生成失败", e);
                        throw new RuntimeException("HMAC签名生成失败", e);
                    }
                })
                .flatMap(next::exchange));
    }

    /**
     * 从request.body()提取请求体内容用于HMAC签名
     *
     * @param request 客户端请求
     * @return 请求体字符串的Mono
     */
    private Mono<String> extractRequestBodyFromBody(ClientRequest request) {
        HttpMethod method = request.method();
        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
            try {
                Optional<Object> attribute = request.attribute("requestData");
                if (attribute.isPresent()) {
                    Object body = attribute.get();
                    if (body instanceof String str) {
                        return Mono.just(str);
                    } else {
                        return Mono.just(GSON.toJson(body));
                    }
                }
            } catch (Exception e) {
                log.warn("从request.attribute()提取 requestData 时发生异常: {}", e.getMessage());
            }
        }
        return Mono.just("");
    }

    /**
     * 提取查询参数
     *
     * @param request 客户端请求
     * @return 查询参数Map
     */
    private Map<String, String[]> extractQueryParams(ClientRequest request) {
        Map<String, String[]> queryParams = new HashMap<>();
        MultiValueMap<String, String> params =
                UriComponentsBuilder.fromUri(request.url()).build().getQueryParams();
        params.forEach((key, values) -> queryParams.put(key, values.toArray(new String[0])));
        return queryParams;
    }
}
