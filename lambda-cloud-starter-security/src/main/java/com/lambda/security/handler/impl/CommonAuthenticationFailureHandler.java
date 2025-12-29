package com.lambda.security.handler.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.cloud.core.exception.model.ErrorModel;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.security.exception.CaptchaRequiredException;
import com.lambda.security.handler.AuthenticationFailureHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * 通用认证失败处理器实现
 * <p>
 * 提供默认的认证失败处理逻辑，支持Ajax请求和普通请求的不同处理方式。
 * 对于Ajax请求返回JSON格式的错误信息，对于普通请求进行页面重定向。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>区分Ajax请求和普通请求</li>
 *   <li>返回标准化的错误响应</li>
 *   <li>支持多种异常类型处理</li>
 *   <li>提供灵活的重定向机制</li>
 * </ul>
 *
 * <h3>处理策略：</h3>
 * <ul>
 *   <li>Ajax请求：返回JSON格式错误信息</li>
 *   <li>普通请求：重定向到错误页面</li>
 *   <li>支持自定义重定向URL</li>
 *   <li>默认重定向到401错误页</li>
 * </ul>
 *
 * <h3>异常处理：</h3>
 * <ul>
 *   <li>NotLoginException - 未登录异常</li>
 *   <li>SaTokenException - Sa-Token异常</li>
 *   <li>其他异常 - 通用认证失败</li>
 * </ul>
 *
 * <h3>响应格式：</h3>
 * <pre>{@code
 * {
 *   "status": 401,
 *   "error": "UNAUTHORIZED",
 *   "message": "认证失败原因",
 *   "path": "/api/login",
 *   "timestamp": 1234567890
 * }
 * }</pre>
 *
 * @author jpjoo
 * @author Lambda Cloud Team
 * @see AuthenticationFailureHandler
 * @see ErrorModel
 * @since 1.0.0
 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP2"},
        justification = "springboot properties")
@SuppressWarnings("all")
public class CommonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    /**
     * JSON序列化工具
     * <p>
     * 用于将错误模型对象序列化为JSON格式的响应数据。
     * </p>
     */
    private final ObjectMapper objectMapper;

    /**
     * 构造通用认证失败处理器
     * <p>
     * 初始化处理器实例，注入JSON序列化工具。
     * </p>
     *
     * @param objectMapper JSON序列化工具，用于生成错误响应
     */
    public CommonAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 处理认证失败事件
     * <p>
     * 根据请求类型（Ajax或普通请求）采用不同的处理策略。
     * Ajax请求返回JSON格式的错误信息，普通请求进行页面重定向。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>判断请求类型（Ajax或普通）</li>
     *   <li>Ajax请求：构建错误模型并返回JSON</li>
     *   <li>普通请求：重定向到错误页面</li>
     *   <li>设置适当的HTTP状态码和响应头</li>
     * </ol>
     *
     * <h3>Ajax请求处理：</h3>
     * <ul>
     *   <li>设置401状态码</li>
     *   <li>设置缓存控制头</li>
     *   <li>构建标准错误模型</li>
     *   <li>返回JSON格式响应</li>
     * </ul>
     *
     * <h3>普通请求处理：</h3>
     * <ul>
     *   <li>检查自定义重定向URL</li>
     *   <li>优先使用自定义URL</li>
     *   <li>默认重定向到/401页面</li>
     * </ul>
     *
     * @param request   HTTP请求对象，用于获取请求信息
     * @param response  HTTP响应对象，用于设置响应内容
     * @param exception 认证失败异常，包含失败原因
     * @throws IOException 当I/O操作失败时抛出
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, Exception exception)
            throws IOException {
        if (WebHttpUtils.isAjaxRequest(request)) {
            // Ajax请求处理：返回JSON格式错误信息
            try (PrintWriter writer = response.getWriter()) {
                // 设置HTTP状态码和响应头
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setHeader("Expires", "0");
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                // 构建错误模型
                ErrorModel errorModel = new ErrorModel();
                errorModel.setStatus(HttpStatus.UNAUTHORIZED.value());

                // 根据异常类型设置错误码
                if (exception instanceof NotLoginException NotLoginException) {
                    errorModel.setError(NotLoginException.getType());
                } else if (exception instanceof SaTokenException saTokenException) {
                    errorModel.setError(String.valueOf(saTokenException.getCode()));
                } else if (exception instanceof CaptchaRequiredException captchaRequiredException) {
                    errorModel.setError(String.valueOf(captchaRequiredException.getCode()));
                    Map<String, Object> data = new HashMap<>();
                    data.put("captchaRequired", true);
                    data.put("currentFailureTimes", captchaRequiredException.getCurrentFailureTimes());
                    data.put("triggerTimes", captchaRequiredException.getTriggerTimes());
                    errorModel.setDetails(data);
                } else {
                    errorModel.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
                }

                // 设置错误详细信息
                errorModel.setMessage(exception.getMessage());
                errorModel.setPath(request.getRequestURI());
                errorModel.setTimestamp(System.currentTimeMillis());

                // 序列化并返回JSON响应
                objectMapper.writeValue(writer, errorModel);
            }
        } else {
            // 普通请求处理：页面重定向
            String redirectUrl = (String) WebHttpUtils.getRedirectAttribute(request);
            if (StringUtils.isNotBlank(redirectUrl)) {
                // 使用自定义重定向URL
                WebHttpUtils.sendRedirect(request, response, redirectUrl);
            } else {
                // 默认重定向到401错误页面
                WebHttpUtils.sendRedirect(request, response, "/401");
            }
        }
    }
}
