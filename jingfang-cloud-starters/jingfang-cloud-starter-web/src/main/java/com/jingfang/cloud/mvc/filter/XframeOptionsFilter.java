package com.jingfang.cloud.mvc.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Jin
 */
@WebFilter(urlPatterns = "*.html")
public class XframeOptionsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("X-Frame-Options", "sameorigin");
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
