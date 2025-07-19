package com.lambda.security.web;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.handler.AuthenticationFailureHandler;
import com.lambda.security.handler.AuthenticationSuccessHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.log.LogMessage;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * AbstractAuthenticationProcessingFilter
 *
 * @author jpjoo
 */
@Slf4j
public abstract class AbstractAuthenticationProcessingFilter extends GenericFilterBean {
    public static final String LOGIN_PARAMETERS = "loginParameters";
    protected static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;

    @Setter
    private String filterProcessesUrl;

    protected AbstractAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        this.filterProcessesUrl = defaultFilterProcessesUrl;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        this.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (this.nonRequiresAuthentication(request)) {
            chain.doFilter(request, response);
        } else {
            try {
                HttpServletRequest wrapRequest = this.wrapRequest(request);
                LoginUser loginUser = this.attemptAuthentication(wrapRequest, response);
                this.successfulAuthentication(wrapRequest, response, chain, loginUser);
            } catch (Exception exception) {
                if (exception instanceof AuthenticationException) {
                    this.unsuccessfulAuthentication(request, response, (AuthenticationException) exception);
                } else {
                    log.error("authentication exception", exception);
                    this.unsuccessfulAuthentication(
                            request, response, new AuthenticationException(exception.getMessage()));
                }
            }
        }
    }

    protected HttpServletRequest wrapRequest(HttpServletRequest request) throws IOException {
        return request;
    }

    protected boolean nonRequiresAuthentication(HttpServletRequest request) {
        if (ANT_PATH_MATCHER.match(this.filterProcessesUrl, request.getRequestURI())) {
            return false;
        } else {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("Did not match request to %s", filterProcessesUrl));
            }
            return true;
        }
    }

    /**
     * 尝试认证
     *
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     * @throws IOException
     * @throws ServletException
     */
    public abstract LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException;

    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain, LoginUser loginUser)
            throws IOException, ServletException {
        this.successHandler.onAuthenticationSuccess(request, response, loginUser);
    }

    protected void unsuccessfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }

    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.successHandler = successHandler;
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        Assert.notNull(failureHandler, "failureHandler cannot be null");
        this.failureHandler = failureHandler;
    }

    protected AuthenticationSuccessHandler getSuccessHandler() {
        return this.successHandler;
    }

    protected AuthenticationFailureHandler getFailureHandler() {
        return this.failureHandler;
    }

    public Map<String, Object> getUserLoginForRequestBody(HttpServletRequest request) {
        try {
            Map<String, Object> loginParameters = WebHttpUtils.getRequestBody(request);
            request.setAttribute(LOGIN_PARAMETERS, loginParameters);
            return loginParameters;
        } catch (Exception e) {
            throw new AuthenticationException("获取用户数据失败！");
        }
    }
}
