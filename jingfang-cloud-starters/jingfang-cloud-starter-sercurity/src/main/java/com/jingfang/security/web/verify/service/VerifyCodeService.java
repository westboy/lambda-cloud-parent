package com.jingfang.security.web.verify.service;

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

    /**
     * 检查是否支持该过滤器
     *
     * @param request
     * @return
     */
    boolean support(HttpServletRequest request);

    /**
     * 执行校验
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;


}
