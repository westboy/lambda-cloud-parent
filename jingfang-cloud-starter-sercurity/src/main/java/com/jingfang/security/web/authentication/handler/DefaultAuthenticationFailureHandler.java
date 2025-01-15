package com.jingfang.security.web.authentication.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingfang.cloud.core.exception.model.ErrorModel;
import com.jingfang.cloud.mvc.WebHttpUtils;
import com.jingfang.security.handler.AuthenticationFailureHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * DefaultAuthenticationFailureHandler
 *
 * @author jpjoo
 */
public class DefaultAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public DefaultAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException {
        if (WebHttpUtils.isAjaxRequest(request)) {
            try (PrintWriter writer = response.getWriter()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setHeader("Expires", "0");
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                ErrorModel model = new ErrorModel();
                model.setStatus(HttpStatus.UNAUTHORIZED.value());
                String error = HttpStatus.UNAUTHORIZED.getReasonPhrase();
                model.setError(error);
                model.setMessage(exception.getMessage());
                model.setPath(request.getRequestURI());
                model.setTimestamp(System.currentTimeMillis());
                objectMapper.writeValue(writer, model);
            }
        } else {
            String redirectUrl = (String) WebHttpUtils.getRedirectAttribute(request);
            if (StringUtils.isNotBlank(redirectUrl)) {
                WebHttpUtils.sendRedirect(request, response, redirectUrl);
            } else {
                WebHttpUtils.sendRedirect(request, response, "/404");
            }
        }
    }
}
