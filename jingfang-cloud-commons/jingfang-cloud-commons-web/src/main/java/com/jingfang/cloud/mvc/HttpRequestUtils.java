package com.jingfang.cloud.mvc;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author Jin
 */
public final class HttpRequestUtils {

    private HttpRequestUtils() {
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
}
