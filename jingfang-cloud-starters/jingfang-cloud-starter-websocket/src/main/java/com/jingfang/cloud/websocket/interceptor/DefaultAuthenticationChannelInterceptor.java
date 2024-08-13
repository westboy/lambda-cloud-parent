package com.jingfang.cloud.websocket.interceptor;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.text.CharSequenceUtil;
import com.google.common.collect.Maps;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.cloud.websocket.Constants;
import com.jingfang.cloud.core.principal.LoginType;
import com.jingfang.security.exception.AuthenticationException;
import com.jingfang.security.service.UserDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.List;
import java.util.Map;
import static com.jingfang.cloud.mvc.WebHttpUtils.AUTHORIZATION;
import static com.jingfang.cloud.mvc.WebHttpUtils.BEARER;

/**
 * DefaultAuthenticationChannelInterceptor
 *
 * @author jpjoo
 */
@Slf4j
public class DefaultAuthenticationChannelInterceptor implements ChannelInterceptor {

    private UserDetailService userDetailService;

    @Autowired
    public void setUserDetailService(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                StompHeaderAccessor.class);
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
            try {
                String accessToken = getAccessToken(accessor);
                if (StringUtils.isNotBlank(accessToken)) {
                    Opt<LoginUser> loginUserOpt = Opt.ofNullable(getLoginUser(LoginType.ADMIN,accessToken))
                            .or(() -> Opt.ofNullable(getLoginUser(LoginType.USER,accessToken)));
                    accessor.setUser(loginUserOpt.orElseThrow(() -> new AuthenticationException("用户不能存在!")));
                } else {
                    throw new AuthenticationException("认证失败！");
                }
            } catch (Exception e) {
                throw new AuthenticationException("认证失败");
            }
        }
        return message;
    }

    private LoginUser getLoginUser(LoginType loginType, String accessToken) {
        String username = loginType.getStpLogic().getLoginIdNotHandle(accessToken);
        if (CharSequenceUtil.isNotEmpty(username)) {
            return userDetailService.loginByUsername(username, loginType.getCode());
        }
        return null;
    }

    public static String getAccessToken(StompHeaderAccessor accessor) {
        List<String> payloads = accessor.getNativeHeader(AUTHORIZATION);
        if (CollectionUtils.isNotEmpty(payloads)) {
            String payload = payloads.get(0);
            if (StringUtils.isNotBlank(payload) && payload.startsWith(BEARER)) {
                return payload.replace(BEARER, StringUtils.EMPTY);
            }
        }
        return null;
    }
}