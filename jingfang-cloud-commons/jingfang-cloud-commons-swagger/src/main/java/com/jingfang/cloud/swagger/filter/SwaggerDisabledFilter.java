package com.jingfang.cloud.swagger.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SwaggerDisabledFilter
 *
 * @author w
 */
public class SwaggerDisabledFilter implements Filter {

    private final String docUri;

    public SwaggerDisabledFilter(String docUri) {
        this.docUri = docUri;
    }

    @Override
    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) arg0;
        HttpServletResponse response = (HttpServletResponse) arg1;
        String uri = request.getRequestURI();
        if (docUri.equals(uri)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            chain.doFilter(request, response);
        }
    }
}