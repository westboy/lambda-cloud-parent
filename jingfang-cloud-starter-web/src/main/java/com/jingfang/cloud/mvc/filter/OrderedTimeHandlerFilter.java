package com.jingfang.cloud.mvc.filter;

import com.jingfang.cloud.web.RequestTimeHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author Jin
 */
@Slf4j
public class OrderedTimeHandlerFilter extends OncePerRequestFilter implements OrderedFilter {


    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RequestTimeHolder.setTime(System.currentTimeMillis());
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestTimeHolder.clear();
        }
    }
}
