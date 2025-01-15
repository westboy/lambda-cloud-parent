package com.jingfang.cloud.web;

import cn.hutool.core.io.IoUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;

/**
 * @author Jin
 *
 * @link org.springframework.web.util.ContentCachingRequestWrapper
 */
@Slf4j
public class DefaultServletRequestWrapper extends HttpServletRequestWrapper {


    private final String body;

    @SneakyThrows
    public DefaultServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.body = IoUtil.readUtf8(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        return new DefaultServletInputStream(body);
    }

    @Override
    public BufferedReader getReader() {
        return IoUtil.getUtf8Reader(this.getInputStream());
    }

}
