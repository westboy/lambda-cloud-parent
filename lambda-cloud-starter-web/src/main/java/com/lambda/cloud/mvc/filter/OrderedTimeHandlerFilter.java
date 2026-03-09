package com.lambda.cloud.mvc.filter;

import com.lambda.cloud.web.RequestTimeHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.servlet.filter.OrderedFilter;
import org.springframework.web.filter.OncePerRequestFilter;

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
    protected void doFilterInternal(
            @Nullable HttpServletRequest request, @Nullable HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        RequestTimeHolder.setTime(System.currentTimeMillis());
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestTimeHolder.clear();
        }
    }
}
