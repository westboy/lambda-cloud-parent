package com.lambda.cloud.webclient.hmac;

import com.lambda.autoconfig.WebClientProperties;
import com.lambda.cloud.core.utils.HmacGenerator;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
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

    @NonNull
    @Override
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {
        if (hmacConfig == null || hmacConfig.getAppId() == null || hmacConfig.getSecret() == null) {
            log.debug("HMAC配置为空，跳过HMAC认证");
            return next.exchange(request);
        }

        return extractRequestBody(request).flatMap(requestBody -> {
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
                return next.exchange(newRequest);

            } catch (UnsupportedEncodingException e) {
                log.error("HMAC签名生成失败", e);
                return Mono.error(new RuntimeException("HMAC签名生成失败", e));
            }
        });
    }

    /**
     * 提取请求体内容用于HMAC签名
     * <p>
     * 根据配置的签名策略决定是否包含请求体内容。
     * 支持从ClientRequest的attributes中获取用户设置的请求体内容。
     * </p>
     *
     * <h3>使用方式：</h3>
     * <pre>{@code
     * // 使用HmacRequestBodyHelper设置请求体
     * String requestBody = "{\"name\":\"test\"}";
     * webClient.post()
     *     .uri("/api/test")
     *     .attribute(HmacRequestBodyHelper.HMAC_REQUEST_BODY_ATTR, requestBody)
     *     .bodyValue(requestBody)
     *     .retrieve();
     * }</pre>
     *
     * @param request 客户端请求
     * @return 请求体字符串的Mono
     */
    private Mono<String> extractRequestBody(ClientRequest request) {
        // 检查配置的签名策略
        if (hmacConfig != null) {
            HttpMethod method = request.method();
            if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
                Object requestBody = request.attributes().get("requestBody");
                if (requestBody instanceof String bodyContent) {
                    log.debug("从请求属性中获取到字符串请求体内容，长度: {} 字符", bodyContent.length());
                    return Mono.just(bodyContent);
                } else if (requestBody instanceof byte[] bodyBytes) {
                    String bodyContent = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
                    log.debug("从请求属性中获取到字节数组请求体内容，长度: {} 字节", bodyBytes.length);
                    return Mono.just(bodyContent);
                } else if (requestBody != null) {
                    log.warn(
                            "请求属性中的请求体内容类型不支持: {}，期望String或byte[]类型",
                            requestBody.getClass().getSimpleName());
                }
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
