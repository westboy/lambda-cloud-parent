package com.jingfang.cloud.mvc.filter;

import com.jingfang.cloud.web.JingfangTimeHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Jin
 */
@Slf4j
public class OrderedTimeHandlerFilter extends OncePerRequestFilter implements OrderedFilter {

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain chain) throws ServletException, IOException {
        JingfangTimeHolder.setTime(System.currentTimeMillis());
        try {
            chain.doFilter(request, response);
        } finally {
            JingfangTimeHolder.clear();
        }
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
