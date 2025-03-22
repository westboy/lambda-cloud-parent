package com.lamuda.security.web.verify.service;

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
     * @param request request
     * @return boolean
     */
    boolean support(HttpServletRequest request);

    /**
     * 执行校验
     *
     * @param request request
     * @param response response
     * @param chain chain
     * @throws IOException IOException
     * @throws ServletException ServletException
     */
    void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;


}
