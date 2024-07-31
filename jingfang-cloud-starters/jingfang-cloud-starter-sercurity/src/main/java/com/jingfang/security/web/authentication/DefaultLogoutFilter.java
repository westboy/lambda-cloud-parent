
package com.jingfang.security.web.authentication;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.security.context.SecurityContextHolder;
import com.jingfang.security.handler.CompositeLogoutHandler;
import com.jingfang.security.handler.LogoutHandler;
import com.jingfang.security.handler.LogoutSuccessHandler;
import org.springframework.core.log.LogMessage;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * DefaultLogoutFilter
 *
 * @author jpjoo
 */
public class DefaultLogoutFilter extends GenericFilterBean {
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final LogoutHandler handler;
    private final LogoutSuccessHandler logoutSuccessHandler;
    private final String filterProcessesUrl;

    public DefaultLogoutFilter(LogoutSuccessHandler logoutSuccessHandler, LogoutHandler... handlers) {
        this.handler = new CompositeLogoutHandler(handlers);
        Assert.notNull(logoutSuccessHandler, "logoutSuccessHandler cannot be null");
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.filterProcessesUrl = "/logout";
    }

    public DefaultLogoutFilter(String filterProcessesUrl, LogoutSuccessHandler logoutSuccessHandler, LogoutHandler... handlers) {
        Assert.notNull(logoutSuccessHandler, "logoutSuccessHandler cannot be null");
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.filterProcessesUrl = filterProcessesUrl;
        this.handler = new CompositeLogoutHandler(handlers);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (this.requiresLogout(request, response)) {
            LoginUser loginUser = SecurityContextHolder.getContext().getPrincipal();
            if (this.logger.isDebugEnabled()) {
                this.logger.debug(LogMessage.format("Logging out [%s]", loginUser));
            }
            this.handler.logout(request, response, loginUser);
            this.logoutSuccessHandler.onLogoutSuccess(request, response, loginUser);
        } else {
            chain.doFilter(request, response);
        }
    }

    protected boolean requiresLogout(HttpServletRequest request, HttpServletResponse response) {
        if (antPathMatcher.match(this.filterProcessesUrl, request.getRequestURI())) {
            return true;
        } else {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("Did not match request to %s", this.filterProcessesUrl));
            }
            return false;
        }
    }
}
