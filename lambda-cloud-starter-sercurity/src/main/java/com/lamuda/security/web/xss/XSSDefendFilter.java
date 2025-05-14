package com.lambda.security.web.xss;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

/**
 * XSSDefendFilter
 *
 * @author Jin
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@SuppressWarnings({"AlibabaClassNamingShouldBeCamel", "PMD"})
public class XSSDefendFilter implements Filter {
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
    private final Set<String> trusted;

    public XSSDefendFilter(Set<String> trusted) {
        System.setProperty("org.owasp.esapi.logSpecial.discard", "true");
        this.trusted = trusted;
    }

    @Override
    @SuppressWarnings("squid:S1874")
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (trusted.stream().anyMatch(item -> ANT_PATH_MATCHER.match(item, httpServletRequest.getRequestURI()))) {
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(new XSSRequestWrapper(httpServletRequest), response);
    }
}
