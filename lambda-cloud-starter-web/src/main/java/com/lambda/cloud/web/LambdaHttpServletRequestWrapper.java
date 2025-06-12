package com.lambda.cloud.web;

import cn.hutool.core.io.IoUtil;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jin
 *
 * @link org.springframework.web.util.ContentCachingRequestWrapper
 */
@Slf4j
public class LambdaHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private final String body;

	@SneakyThrows
	public LambdaHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		this.body = IoUtil.readUtf8(request.getInputStream());
	}

	@Override
	public ServletInputStream getInputStream() {
		return new LambdaServletInputStream(body);
	}

	@Override
	public BufferedReader getReader() {
		return IoUtil.getUtf8Reader(this.getInputStream());
	}
}
