package com.lambda.security.handler.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.cloud.core.exception.model.ErrorModel;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.security.handler.AuthenticationFailureHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * DefaultAuthenticationFailureHandler
 *
 * @author jpjoo
 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP2"},
        justification = "springboot properties")
@SuppressWarnings("all")
public class CommonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public CommonAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, Exception exception)
            throws IOException {
        if (WebHttpUtils.isAjaxRequest(request)) {
            try (PrintWriter writer = response.getWriter()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setHeader("Expires", "0");
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                ErrorModel errorModel = new ErrorModel();
                errorModel.setStatus(HttpStatus.UNAUTHORIZED.value());
                if (exception instanceof NotLoginException NotLoginException) {
                    errorModel.setError(NotLoginException.getType());
                } else if (exception instanceof SaTokenException saTokenException) {
                    errorModel.setError(String.valueOf(saTokenException.getCode()));
                } else {
                    errorModel.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
                }
                errorModel.setMessage(exception.getMessage());
                errorModel.setPath(request.getRequestURI());
                errorModel.setTimestamp(System.currentTimeMillis());
                objectMapper.writeValue(writer, errorModel);
            }
        } else {
            String redirectUrl = (String) WebHttpUtils.getRedirectAttribute(request);
            if (StringUtils.isNotBlank(redirectUrl)) {
                WebHttpUtils.sendRedirect(request, response, redirectUrl);
            } else {
                WebHttpUtils.sendRedirect(request, response, "/401");
            }
        }
    }
}
