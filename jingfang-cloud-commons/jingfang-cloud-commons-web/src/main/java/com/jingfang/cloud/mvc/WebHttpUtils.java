package com.jingfang.cloud.mvc;

import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Jin
 */
public final class WebHttpUtils {

    public static final String XML_HTTP_REQUEST = "x-requested-with";
    public static final String XML_HTTP_REQUEST_VALUE = "XMLHttpRequest";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_VALUE = "application/json";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String X_AUTHORIZED_TOKEN = "x-authorized-token";
    public static final String X_AUTHORIZED_BEARER = X_AUTHORIZED_TOKEN;
    public static final String HMAC = "HmacSHA ";
    private WebHttpUtils() {
    }

    /**
     * 获取当前HTTP请求的HttpServletRequest
     */
    public static HttpServletRequest getCurrentRequest() {
        final RequestAttributes requestAttributes = Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * 获取指定key
     * @param key 标记
     * @return 结果
     */
    public static Object getRequestAttributes(String key) {
        return getCurrentRequest().getAttribute(key);
    }

    /**
     * 从json请求体获取参数
     *
     * @param request
     * @return Map
     */
    public static Map<String, Object> getRequestBody(HttpServletRequest request) {
        try {
            String body = ServletUtil.getBody(request);
            return JSONUtil.parseObj(body);
        } catch (Exception e) {
            return Maps.newHashMap();
        }
    }
    /**
     * 从表单获取参数
     *
     * @param request
     * @return
     */
    public static Map<String, Object> getFormRequest(HttpServletRequest request) {
        List<String> parameterNames = Collections.list(request.getParameterNames());
        return parameterNames.stream().collect(Collectors.toMap(k -> k, request::getParameter));
    }


    public static boolean isAjaxRequest(HttpServletRequest request) {
        String isAjax = request.getHeader(XML_HTTP_REQUEST);
        if (XML_HTTP_REQUEST_VALUE.equals(isAjax)) {
            return true;
        }
        String contentType = request.getHeader(CONTENT_TYPE);
        return StringUtils.isNotBlank(contentType) && contentType.startsWith(CONTENT_TYPE_VALUE);
    }

    public static boolean isBearerRequest(HttpServletRequest request) {
        String bearer = request.getHeader(AUTHORIZATION);
        if (StringUtils.isNotBlank(bearer) && bearer.startsWith(BEARER)) {
            return true;
        }
        Cookie cookie = WebUtils.getCookie(request, X_AUTHORIZED_BEARER);
        return cookie != null && StringUtils.isNotBlank(cookie.getValue());
    }

    public static boolean isNotBearerRequest(HttpServletRequest request) {
        return !isBearerRequest(request);
    }

    public static boolean isHmacRequest(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        return StringUtils.isNotBlank(header) && header.startsWith(HMAC);
    }

    public static boolean isNotHmacRequest(HttpServletRequest request) {
        return !isHmacRequest(request);
    }

}
