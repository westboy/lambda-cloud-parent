package com.lambda.security.web.verify.service.sms;


import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONObject;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.web.LambdaServletRequestWrapper;
import com.lambda.security.LoginMode;
import com.lambda.security.exception.VerifyCodeValidationException;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.service.sms.model.SmsVerifyCode;
import com.lambda.security.web.verify.service.sms.store.SmsVerifyCodeStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

/**
 * 图形验证码校验过滤器
 *
 * @author jpjoo
 */
@SuppressWarnings("all")
public class SmsVerifyCodeValidationImpl implements VerifyCodeService {
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final SecurityProperties securityProperties;
    private final SmsVerifyCodeStore<String> smsVerifyCodeStore;
    @Setter
    private String loginModeParameter = "loginMode";

    public SmsVerifyCodeValidationImpl(SecurityProperties securityProperties, SmsVerifyCodeStore<String> smsVerifyCodeStore) {
        this.securityProperties = securityProperties;
        this.smsVerifyCodeStore = smsVerifyCodeStore;
    }


    @Override
    public boolean support(HttpServletRequest request) {
        final SecurityProperties.SmsLogin smsLogin = securityProperties.getSms();
        return smsLogin.isEnabled()
                && JakartaServletUtil.isPostMethod(request)
                && matcher.match(smsLogin.getLoginPath(), request.getRequestURI());
    }

    @Override
    public void execute(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain) throws ServletException, IOException {
        LambdaServletRequestWrapper requestWrapper = getRequestWrapper(httpServletRequest);
        JSONObject requestParam = getRequestParam(requestWrapper);
        if (MapUtils.isEmpty(requestParam)) {
            chain.doFilter(requestWrapper, httpServletResponse);
            return;
        }

        String loginMode = requestParam.getStr(loginModeParameter);

        if (StrUtil.isEmpty(loginMode)) {
            throw new VerifyCodeValidationException("登录模式不能为空!");
        }

        if (!LoginMode.SMS.getCode().equals(loginMode)) {
            chain.doFilter(requestWrapper, httpServletResponse);
            return;
        }

        String mobile = requestParam.getStr(securityProperties.getSms().getMobile());
        if (StringUtils.isBlank(mobile)) {
            throw new VerifyCodeValidationException("mobile is not blank!");
        }
        String code = requestParam.getStr(securityProperties.getSms().getCode());
        if (StringUtils.isBlank(code)) {
            throw new VerifyCodeValidationException("code is not blank!");
        }

        SmsVerifyCode<String> verifyCode = smsVerifyCodeStore.get(mobile);

        this.checkValid(verifyCode);

        boolean verified = smsVerifyCodeStore.verify(mobile, code);
        if (!verified) {
            throw new VerifyCodeValidationException("code is not valid");
        }
        chain.doFilter(requestWrapper, httpServletResponse);
    }

    public String obtainMobileParameter(HttpServletRequest request) {
        return request.getParameter(securityProperties.getSms().getMobile());
    }

    public String obtainCodeParameter(HttpServletRequest request) {
        return request.getParameter(securityProperties.getSms().getCode());
    }

    private void checkValid(SmsVerifyCode<String> code) {
        int sumSeconds = securityProperties.getSms().getValidMinutes() * VerifyCodeService.ONE_SECOND;
        boolean b = code.getCreateTimeMillis() + sumSeconds > System.currentTimeMillis();
        Assert.isTrue(b, "verify code invalid");
    }

}
