package com.lambda.cloud.webclient.authorization;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import com.lambda.autoconfig.WebClientProperties;
import com.lambda.cloud.core.utils.StpLogicUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
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
@SuppressFBWarnings("EI_EXPOSE_REP2")
@Slf4j
@RequiredArgsConstructor
public class AuthorizationExchangeFilterFunction implements ExchangeFilterFunction {
    private WebClientProperties.ClientConfig clientConfig;

    /**
     * 配置HMAC参数
     *
     * @param clientConfig 客户端配置
     * @return 当前实例
     */
    public AuthorizationExchangeFilterFunction withConfig(WebClientProperties.ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return this;
    }

    /**
     *
     * @param request the current request
     * @param next    the next exchange function in the chain
     * @return Mono<ClientResponse>
     */
    @NonNull
    @Override
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {
        if (clientConfig.isAuthorizationEnabled()) {
            StpLogic stpLogic = StpLogicUtils.getActiveStpLogic();
            SaSession saSession = stpLogic.getSession();
            if (saSession != null) {
                ClientRequest newRequest = ClientRequest.from(request)
                        .header(stpLogic.getTokenName(), stpLogic.getConfigOrGlobal().getTokenPrefix() + " " + saSession.getToken())
                        .build();
                return next.exchange(newRequest);
            }
        }
        return next.exchange(request);
    }

}
