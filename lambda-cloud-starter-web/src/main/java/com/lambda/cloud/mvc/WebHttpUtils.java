package com.lambda.cloud.mvc;

import static java.nio.charset.StandardCharsets.UTF_8;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

/**
 * @author Jin
 */
@Slf4j
public final class WebHttpUtils {

    private static final Pattern ABSOLUTE_URL = Pattern.compile("\\A[a-z0-9.+-]+://.*", Pattern.CASE_INSENSITIVE);
    public static final String XML_HTTP_REQUEST = "x-requested-with";
    public static final String XML_HTTP_REQUEST_VALUE = "XMLHttpRequest";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_VALUE = "application/json";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String X_AUTHORIZED_TOKEN = "x-authorized-token";
    public static final String X_AUTHORIZED_BEARER = X_AUTHORIZED_TOKEN;
    public static final String HMAC = "HmacSHA ";
    public static final String REDIRECT_URL = "__redirectUrl";

    private WebHttpUtils() {}

    public static String getRedirectParameter(HttpServletRequest request, String tokens)
            throws UnsupportedEncodingException {
        Object redirectUrl = request.getAttribute(REDIRECT_URL);
        if (redirectUrl != null && StringUtils.isNotBlank(redirectUrl.toString())) {
            String redirect = redirectUrl.toString();
            String symbol = redirect.contains("?") ? "&" : "?";
            return redirect + symbol + "auth=" + URLEncoder.encode(tokens, UTF_8);
        }
        return null;
    }

    public static Object getRedirectAttribute(HttpServletRequest request) {
        return request.getAttribute(REDIRECT_URL);
    }

    public static void addJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(X_AUTHORIZED_BEARER, token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    public static void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(X_AUTHORIZED_BEARER, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    public static void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
            throws IOException {
        String redirectUrl = calculateRedirectUrl(request.getContextPath(), url, false);
        redirectUrl = response.encodeRedirectURL(redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    public static void sendRedirect(
            HttpServletRequest request, HttpServletResponse response, String url, boolean contextRelative)
            throws IOException {
        String redirectUrl = calculateRedirectUrl(request.getContextPath(), url, contextRelative);
        redirectUrl = response.encodeRedirectURL(redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private static String calculateRedirectUrl(String contextPath, String url, boolean contextRelative) {
        if (!isAbsoluteUrl(url)) {
            return contextRelative ? url : contextPath + url;
        } else if (!contextRelative) {
            return url;
        } else {
            Assert.isTrue(url.contains(contextPath), "The fully qualified URL does not include context path.");
            url = url.substring(url.lastIndexOf("://") + 3);
            url = url.substring(url.indexOf(contextPath) + contextPath.length());
            if (url.length() > 1 && url.charAt(0) == '/') {
                url = url.substring(1);
            }
            return url;
        }
    }

    public static HttpServletRequest getCurrentRequest() {
        final RequestAttributes requestAttributes = Objects.requireNonNull(RequestContextHolder.getRequestAttributes());
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public static Object getRequestAttributes(String key) {
        return getCurrentRequest().getAttribute(key);
    }

    public static Map<String, Object> getRequestBody(HttpServletRequest request) {
        try {
            String body = JakartaServletUtil.getBody(request);
            return JSONUtil.parseObj(body);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

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

    public static String buildFullRequestUrl(HttpServletRequest r) {
        return buildFullRequestUrl(
                r.getScheme(), r.getServerName(), r.getServerPort(), r.getRequestURI(), r.getQueryString());
    }

    public static String buildFullRequestUrl(
            String scheme, String serverName, int serverPort, String requestURI, String queryString) {
        scheme = scheme.toLowerCase();
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if ("http".equals(scheme)) {
            if (serverPort != 80) {
                url.append(":").append(serverPort);
            }
        } else if ("https".equals(scheme) && serverPort != 443) {
            url.append(":").append(serverPort);
        }

        url.append(requestURI);
        if (queryString != null) {
            url.append("?").append(queryString);
        }

        return url.toString();
    }

    public static String buildRequestUrl(HttpServletRequest r) {
        return buildRequestUrl(
                r.getServletPath(), r.getRequestURI(), r.getContextPath(), r.getPathInfo(), r.getQueryString());
    }

    private static String buildRequestUrl(
            String servletPath, String requestURI, String contextPath, String pathInfo, String queryString) {
        StringBuilder url = new StringBuilder();
        if (servletPath != null) {
            url.append(servletPath);
            if (pathInfo != null) {
                url.append(pathInfo);
            }
        } else {
            url.append(requestURI.substring(contextPath.length()));
        }

        if (queryString != null) {
            url.append("?").append(queryString);
        }

        return url.toString();
    }

    public static boolean isValidRedirectUrl(String url) {
        return url != null && (url.startsWith("/") || isAbsoluteUrl(url));
    }

    public static boolean isAbsoluteUrl(String url) {
        return url != null && ABSOLUTE_URL.matcher(url).matches();
    }
}
