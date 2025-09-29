package com.lambda.cloud.webclient;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WebClient通用服务类
 *
 * @author jpjoo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebClientTemplate {

    private final WebClientTemplateFactory webClientTemplateFactory;

    /**
     * GET请求 - 返回字符串
     */
    public Mono<String> get(String url) {
        return get(url, String.class);
    }

    /**
     * GET请求 - 返回指定类型
     */
    public <E> Mono<E> get(String url, Class<E> responseType) {
        return get("default", url, responseType);
    }

    /**
     * GET请求 - 使用指定客户端
     */
    public <E> Mono<E> get(String clientName, String url, Class<E> responseType) {
        return webClientTemplateFactory
                .create(clientName)
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(responseType)
                .doOnError(this::logError);
    }

    /**
     * GET请求 - 带参数
     */
    public <E> Mono<E> get(String url, Map<String, Object> params, Class<E> responseType) {
        return get("default", url, params, responseType);
    }

    /**
     * GET请求 - 使用指定客户端，带参数
     */
    public <E> Mono<E> get(String clientName, String url, Map<String, Object> params, Class<E> responseType) {
        WebClient.RequestHeadersUriSpec<?> spec =
                webClientTemplateFactory.create(clientName).get();
        if (params != null && !params.isEmpty()) {
            spec.uri(uriBuilder -> {
                UriBuilder builder = uriBuilder.path(url);
                params.forEach(builder::queryParam);
                return builder.build();
            });
        } else {
            spec.uri(url);
        }
        return spec.retrieve().bodyToMono(responseType).doOnError(this::logError);
    }

    /**
     * POST请求 - JSON请求体
     */
    public <E> Mono<E> post(String url, Object requestBody, Class<E> responseType) {
        return post("default", url, requestBody, responseType);
    }

    /**
     * POST请求 - 使用指定客户端
     */
    public <E> Mono<E> post(String clientName, String url, Object requestBody, Class<E> responseType) {
        return webClientTemplateFactory
                .create(clientName)
                .post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .doOnError(this::logError);
    }

    /**
     * POST请求 - 表单数据
     */
    public <E> Mono<E> post(String url, MultiValueMap<String, String> formData, Class<E> responseType) {
        return post("default", url, formData, responseType);
    }

    /**
     * POST请求 - 使用指定客户端，表单数据
     */
    public <E> Mono<E> post(
            String clientName, String url, MultiValueMap<String, String> formData, Class<E> responseType) {
        return webClientTemplateFactory
                .create(clientName)
                .post()
                .uri(url)
                .attribute("requestData", formData)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(responseType)
                .doOnError(this::logError);
    }

    /**
     * PUT请求
     */
    public <E> Mono<E> put(String url, Object requestBody, Class<E> responseType) {
        return put("default", url, requestBody, responseType);
    }

    /**
     * PUT请求 - 使用指定客户端
     */
    public <E> Mono<E> put(String clientName, String url, Object requestBody, Class<E> responseType) {
        ParameterizedTypeReference<E> typeRef = ParameterizedTypeReference.forType(responseType);
        return webClientTemplateFactory
                .create(clientName)
                .put()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .attribute("requestData", requestBody)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(typeRef)
                .doOnError(this::logError);
    }

    /**
     * DELETE请求
     */
    public <E> Mono<E> delete(String url, Class<E> responseType) {
        return delete("default", url, responseType);
    }

    /**
     * DELETE请求 - 使用指定客户端
     */
    public <E> Mono<E> delete(String clientName, String url, Class<E> responseType) {
        ParameterizedTypeReference<E> typeRef = ParameterizedTypeReference.forType(responseType);
        return webClientTemplateFactory
                .create(clientName)
                .delete()
                .uri(url)
                .retrieve()
                .bodyToMono(typeRef)
                .doOnError(this::logError);
    }

    /**
     * 通用请求方法
     */
    public <E> Mono<E> exchange(
            String clientName,
            HttpMethod method,
            String url,
            Object requestBody,
            Class<E> responseType,
            Consumer<HttpHeaders> headersConsumer) {
        ParameterizedTypeReference<E> typeRef = ParameterizedTypeReference.forType(responseType);
        WebClient.RequestBodyUriSpec spec = (WebClient.RequestBodyUriSpec)
                webClientTemplateFactory.create(clientName).method(method).uri(url);
        if (headersConsumer != null) {
            spec.headers(headersConsumer);
        }
        if (requestBody != null) {
            spec.contentType(MediaType.APPLICATION_JSON).bodyValue(requestBody);
        }
        return spec.retrieve().bodyToMono(typeRef).doOnError(this::logError);
    }

    /**
     * 流式请求 - 返回Flux
     */
    public <E> Flux<E> getStream(String url, Class<E> responseType) {
        return getStream("default", url, responseType);
    }

    /**
     * 流式请求 - 使用指定客户端
     */
    public <E> Flux<E> getStream(String clientName, String url, Class<E> responseType) {
        ParameterizedTypeReference<E> typeRef = ParameterizedTypeReference.forType(responseType);
        return webClientTemplateFactory
                .create(clientName)
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(typeRef)
                .doOnError(this::logError);
    }

    /**
     * 带超时的请求
     */
    public <E> Mono<E> getWithTimeout(String url, Class<E> responseType, Duration timeout) {
        return get(url, responseType).timeout(timeout);
    }

    /**
     * 错误日志记录
     */
    private void logError(Throwable error) {
        if (error instanceof WebClientResponseException ex) {
            log.error("WebClient request failed: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
        } else {
            log.error("WebClient request failed", error);
        }
    }
}
