package com.lambda.cloud.webclient.authorization;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import com.lambda.cloud.core.utils.StpLogicUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * Authorization认证过滤器
 * <p>
 * 该过滤器会在请求发送前自动添加Authorization Token认证头。
 * </p>
 *
 * @author jpjoo
 */
@Slf4j
@RequiredArgsConstructor
public class AuthorizationExchangeFilterFunction implements ExchangeFilterFunction {

    /**
     *
     * @param request the current request
     * @param next    the next exchange function in the chain
     * @return Mono<ClientResponse>
     */
    @NonNull
    @Override
    public Mono<ClientResponse> filter(@NonNull ClientRequest request, @NonNull ExchangeFunction next) {
        StpLogic stpLogic = StpLogicUtils.getActiveStpLogic();
        SaSession saSession = stpLogic.getSession();
        if (saSession != null) {
            ClientRequest newRequest = ClientRequest.from(request)
                    .header(
                            stpLogic.getTokenName(),
                            stpLogic.getConfigOrGlobal().getTokenPrefix() + " " + saSession.getToken())
                    .build();
            return next.exchange(newRequest);
        } else {
            return next.exchange(request);
        }
    }
}
