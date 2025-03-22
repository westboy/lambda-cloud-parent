package com.lamuda.cloud.gateway.utils;

import com.google.gson.Gson;
import com.lamuda.cloud.core.exception.model.ErrorModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;

/**
 * WebFlux 工具类
 *
 * @author westboy
 */
@Slf4j
public class WebFluxUtils {

    /**
     * 获取原请求路径
     */
    public static String getOriginalRequestUrl(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        LinkedHashSet<URI> uris = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, new LinkedHashSet<>());
        URI requestUri = uris.stream().findFirst().orElse(request.getURI());
        return UriComponentsBuilder.fromPath(requestUri.getRawPath()).build().toUriString();
    }

    /**
     * 是否是Json请求
     *
     * @param exchange HTTP请求
     */
    public static boolean isJsonRequest(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        return StringUtils.startsWithIgnoreCase(header, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * 读取request内的body
     * <p>
     * 注意一个request只能读取一次 读取之后需要重新包装
     */
    public static String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest) {
        // 获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();
        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(buffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
            DataBufferUtils.release(buffer);
            bodyRef.set(charBuffer.toString());
        });
        return bodyRef.get();
    }

    /**
     * 从缓存中读取request内的body
     * <p>
     * 注意要求经过 {@link ServerWebExchangeUtils#cacheRequestBody(ServerWebExchange, Function)} 此方法创建缓存
     *
     * @return body
     */
    public static String resolveBodyFromCacheRequest(ServerWebExchange exchange) {
        Object obj = exchange.getAttributes().get(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
        if (obj == null) {
            return null;
        }
        DataBuffer buffer = (DataBuffer) obj;
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.toByteBuffer());
        return charBuffer.toString();
    }

    /**
     * 设置webflux模型响应
     *
     * @param response ServerHttpResponse
     * @param status   http状态码
     * @param value    响应内容
     * @return Mono<Void>
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, String path, HttpStatus status, Object value) {
        return webFluxResponseWriter(response, MediaType.APPLICATION_JSON_VALUE, path, status, value);
    }

    /**
     * 设置webflux模型响应
     *
     * @param response    ServerHttpResponse
     * @param contentType content-type
     * @param status      http状态码
     * @param value       响应内容
     * @return Mono<Void>
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, String contentType, String path, HttpStatus status, Object value) {
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, contentType);
        ErrorModel errorModel = new ErrorModel();
        errorModel.setError(status.toString());
        errorModel.setStatus(status.value());
        errorModel.setMessage(value.toString());
        errorModel.setPath(path);
        DataBuffer dataBuffer = response.bufferFactory().wrap(new Gson().toJson(errorModel).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * 重写地址
     *
     * @param exchange
     * @param uri
     */
    public static void rewriteExchangeUrl(@NonNull ServerWebExchange exchange, @NonNull URI uri) {
        Objects.requireNonNull(uri);
        Objects.requireNonNull(exchange);
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route != null) {
            Objects.requireNonNull(route.getPredicate());
            Route replaced = Route.async().id(route.getId())
                    .asyncPredicate(route.getPredicate())
                    .filters(route.getFilters())
                    .order(route.getOrder())
                    .uri(uri).build();
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, replaced);
            log.debug("uri: {}", uri);
        }
    }

    /**
     * 读取请求体
     *
     * @param exchange
     * @return java.lang.String
     */
    public static String readRequestBody(@NonNull ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        return readRequestBody(request);
    }

    /**
     * 读取请求体
     *
     * @param request
     * @return
     */
    public static String readRequestBody(@NonNull ServerHttpRequest request) {
        StringBuilder body = new StringBuilder();
        request.getBody().subscribe(buffer -> {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            DataBufferUtils.release(buffer);
            body.append(new String(bytes, StandardCharsets.UTF_8));
        });
        return body.toString();
    }
}
