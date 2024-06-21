package com.jingfang.security.web;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jingfang.cloud.core.principal.Principal;
import com.jingfang.security.context.SecurityContext;
import com.jingfang.security.context.SecurityContextHolder;
import com.jingfang.security.exception.AuthenticationException;
import com.jingfang.security.handler.AuthenticationFailureHandler;
import com.jingfang.security.handler.AuthenticationSuccessHandler;
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
        if (!this.requiresAuthentication(request, response)) {
            chain.doFilter(request, response);
        } else {
            try {
                Principal principal = this.attemptAuthentication(request, response);
                this.successfulAuthentication(request, response, chain, principal);
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

    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (ANT_PATH_MATCHER.match(this.filterProcessesUrl, request.getRequestURI())) {
            return true;
        } else {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("Did not match request to %s", filterProcessesUrl));
            }
            return false;
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
    public abstract Principal attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException;

    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Principal principal) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setPrincipal(principal);
        SecurityContextHolder.setContext(context);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", principal));
        }
        this.successHandler.onAuthenticationSuccess(request, response, principal);
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
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
