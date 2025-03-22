package com.lamuda.security.web;

import com.lamuda.cloud.core.principal.LoginUser;
import com.lamuda.security.exception.AuthenticationException;
import com.lamuda.security.handler.AuthenticationFailureHandler;
import com.lamuda.security.handler.AuthenticationSuccessHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.log.LogMessage;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * AbstractAuthenticationProcessingFilter
 *
 * @author jpjoo
 */
@Slf4j
public abstract class AbstractAuthenticationProcessingFilter extends GenericFilterBean {
    protected static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
    private AuthenticationSuccessHandler successHandler;
    private AuthenticationFailureHandler failureHandler;
    @Setter
    private String filterProcessesUrl;

    protected AbstractAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        this.filterProcessesUrl = defaultFilterProcessesUrl;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (this.nonRequiresAuthentication(request)) {
            chain.doFilter(request, response);
        } else {
            try {
                HttpServletRequest wrapRequest = this.wrapRequest(request);
                LoginUser loginUser = this.attemptAuthentication(wrapRequest, response);
                this.successfulAuthentication(request, response, chain, loginUser);
            } catch (Exception exception) {
                if (exception instanceof AuthenticationException) {
                    this.unsuccessfulAuthentication(request, response, (AuthenticationException) exception);
                } else {
                    log.error("authentication exception", exception);
                    this.unsuccessfulAuthentication(request, response, new AuthenticationException(exception.getMessage()));
                }
            }
        }
    }

    protected HttpServletRequest wrapRequest(HttpServletRequest request) throws IOException {
        return request;
    }

    protected boolean doNextFilter() {
        return false;
    }

    protected boolean nonRequiresAuthentication(HttpServletRequest request) throws IOException {
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
    public abstract LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException;

    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, LoginUser loginUser) throws IOException, ServletException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", loginUser));
        }
        this.successHandler.onAuthenticationSuccess(request, response, loginUser);
        if (this.doNextFilter()) {
            chain.doFilter(request, response);
        }
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        this.logger.trace("Failed to process authentication request", failed);
        this.logger.trace("Cleared SecurityContextHolder");
        this.logger.trace("Handling authentication failure");
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
}
