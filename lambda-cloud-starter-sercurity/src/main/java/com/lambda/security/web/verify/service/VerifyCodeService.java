package com.lambda.security.web.verify.service;

import com.lambda.cloud.web.DefaultServletRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * VerifyService
 *
 * @author jpjoo
 */
public interface VerifyCodeService {
    Integer ONE_SECOND = 1000;

    /**
     * 检查是否支持该过滤器
     *
     * @param request request
     * @return boolean
     */
    boolean support(HttpServletRequest request);

    /**
     * 执行校验
     *
     * @param request  request
     * @param response response
     * @param chain    chain
     * @throws IOException      IOException
     * @throws ServletException ServletException
     */
    void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;

    /**
     * 获取包装后的request
     *
     * @param request request
     * @return DefaultServletRequestWrapper
     */
    default DefaultServletRequestWrapper getRequestWrapper(HttpServletRequest request) {
        return new DefaultServletRequestWrapper(request);
    }
}
