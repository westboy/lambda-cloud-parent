package com.lambda.cloud.websocket.interceptor;

import static com.lambda.cloud.mvc.WebHttpUtils.AUTHORIZATION;
import static com.lambda.cloud.mvc.WebHttpUtils.BEARER;

import cn.dev33.satoken.session.SaSession;
import cn.hutool.core.lang.Opt;
import com.google.common.collect.Maps;
import com.lambda.cloud.core.principal.LoginType;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.websocket.Constants;
import com.lambda.security.exception.AuthenticationException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

/**
 * DefaultAuthenticationChannelInterceptor
 *
 * @author jpjoo
 */
@Slf4j
public class DefaultAuthenticationChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String framework = accessor.getFirstNativeHeader(Constants.X_WEBSOCKET_FRAMEWORK);
            if (StringUtils.isNotBlank(framework)) {
                accessor.setHeaderIfAbsent(Constants.X_WEBSOCKET_FRAMEWORK, framework);
                Map<String, Object> sessions = accessor.getSessionAttributes();
                if (sessions == null) {
                    sessions = Maps.newHashMap();
                    sessions.put(Constants.X_WEBSOCKET_FRAMEWORK, framework);
                    accessor.setSessionAttributes(sessions);
                } else {
                    sessions.putIfAbsent(Constants.X_WEBSOCKET_FRAMEWORK, framework);
                }
            }
            String accessToken = getAccessToken(accessor);
            if (StringUtils.isNotBlank(accessToken)) {
                Opt<LoginUser> loginUserOpt = Opt.ofNullable(getLoginUser(LoginType.ADMIN, accessToken))
                        .or(() -> Opt.ofNullable(getLoginUser(LoginType.USER, accessToken)));
                accessor.setUser(loginUserOpt.orElseThrow(() -> new AuthenticationException("用户不能存在!")));
            } else {
                throw new AuthenticationException("认证失败！");
            }
        }
        return message;
    }

    private LoginUser getLoginUser(LoginType loginType, String accessToken) {
        SaSession tokenSessionByToken = loginType.getStpLogic().getTokenSessionByToken(accessToken);
        return (LoginUser) tokenSessionByToken.get("loginUser");
    }

    public static String getAccessToken(StompHeaderAccessor accessor) {
        List<String> payloads = accessor.getNativeHeader(AUTHORIZATION);
        if (CollectionUtils.isNotEmpty(payloads)) {
            String payload = payloads.getFirst();
            if (StringUtils.isNotBlank(payload) && payload.startsWith(BEARER)) {
                return payload.replace(BEARER, StringUtils.EMPTY);
            }
        }
        return null;
    }
}
