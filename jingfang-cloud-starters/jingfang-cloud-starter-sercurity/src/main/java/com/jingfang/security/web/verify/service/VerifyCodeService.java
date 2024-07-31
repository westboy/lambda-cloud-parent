package com.jingfang.security.web.verify.service;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
