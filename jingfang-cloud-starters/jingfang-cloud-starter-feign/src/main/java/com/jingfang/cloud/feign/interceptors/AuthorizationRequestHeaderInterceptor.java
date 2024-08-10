package com.jingfang.cloud.feign.interceptors;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.PriorityOrdered;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import java.util.Collection;
import java.util.Map;



/**
 * @author westboy
 */
@Slf4j
public class AuthorizationRequestHeaderInterceptor implements RequestInterceptor, PriorityOrdered {
    public static final String X_AUTHORIZED_TOKEN = "x-authorized-token";
    public static final String X_SECURITY_POLICY = "x-security-policy";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String BEARER = "Bearer ";

    @Override
    public void apply(RequestTemplate template) {
        Map<String, Collection<String>> headers = template.headers();
        boolean state0 = headers.containsKey(CONTENT_TYPE);
        if (!state0) {
            template.header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        }
        boolean state1 = headers.containsKey(X_SECURITY_POLICY);
        boolean state2 = headers.containsKey(AUTHORIZATION);
        if (state1 && state2) {
            template.removeHeader(AUTHORIZATION);
        } else if (!(state1 || state2)) {
            String authorization = getAuthorization();
            if (StringUtils.isNotBlank(authorization)) {
                template.header(AUTHORIZATION, authorization);
            }
        }
    }


    private String getAuthorization() {
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            String authorization = request.getHeader(AUTHORIZATION);
            if (StringUtils.isNotBlank(authorization)) {
                return authorization;
            } else {
                Cookie cookie = WebUtils.getCookie(request, X_AUTHORIZED_TOKEN);
                if (cookie != null && StringUtils.isNotBlank(cookie.getValue())) {
                    return BEARER + cookie.getValue();
                }
            }
        }
        return null;
    }

    @Nullable
    private HttpServletRequest getHttpServletRequest() {
        try {
            RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
            if (ClassUtils.isAssignableValue(ServletRequestAttributes.class, attributes)) {
                return ((ServletRequestAttributes) attributes).getRequest();
            }
        } catch (IllegalStateException ignored) {
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
