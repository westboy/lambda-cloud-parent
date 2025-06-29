package com.lambda.security.web.verify.service;

import cn.hutool.json.JSONObject;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.cloud.web.LambdaHttpServletRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

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
     * @param request  request
     * @param response response
     * @param chain    chain
     * @throws IOException      IOException
     * @throws ServletException ServletException
     */
    void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException;

    /**
     * 获取包装后的request
     *
     * @param request request
     * @return DefaultServletRequestWrapper
     */
    default LambdaHttpServletRequestWrapper getRequestWrapper(HttpServletRequest request) {
        return new LambdaHttpServletRequestWrapper(request);
    }

    /**
     * 获取请求参数
     *
     * @param request LambdaServletRequestWrapper
     * @return JSONObject
     */
    default JSONObject getRequestParam(LambdaHttpServletRequestWrapper request) {
        Map<String, Object> formRequest = WebHttpUtils.getFormRequest(request);
        Map<String, Object> ajaxRequest = WebHttpUtils.getRequestBody(request);
        ajaxRequest.putAll(formRequest);
        return (JSONObject) ajaxRequest;
    }
}
