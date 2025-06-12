package com.lambda.cloud.mvc.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Jin
 */
public class XframeOptionsFilter implements Filter {

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		response.setHeader("X-Frame-Options", "sameorigin");
		filterChain.doFilter(servletRequest, servletResponse);
	}
}
