package com.jingfang.security.web.verify;

import com.jingfang.security.exception.AuthenticationException;
import com.jingfang.security.handler.AuthenticationFailureHandler;
import com.jingfang.security.web.verify.service.VerifyCodeService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Jin
 */
public class VerifyCodeFilter extends GenericFilterBean implements InitializingBean {

    private final List<VerifyCodeService> verifyCodeServices;

    private AuthenticationFailureHandler failureHandler;

    public VerifyCodeFilter(List<VerifyCodeService> verifyCodeServices) {
        if (CollectionUtils.isEmpty(verifyCodeServices)) {
            this.verifyCodeServices = new ArrayList<>();
        } else {
            this.verifyCodeServices = verifyCodeServices;
        }
    }

    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        Assert.notNull(failureHandler, "failureHandler cannot be null");
        this.failureHandler = failureHandler;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        try {
            for (VerifyCodeService service : this.verifyCodeServices) {
                if (service.support(request)) {
                    service.execute(request, response, chain);
                    return;
                }
            }
        } catch (Exception exception) {
            this.failureHandler.onAuthenticationFailure(request, response, exception);
            return;
        }
        chain.doFilter(request, response);
    }


}