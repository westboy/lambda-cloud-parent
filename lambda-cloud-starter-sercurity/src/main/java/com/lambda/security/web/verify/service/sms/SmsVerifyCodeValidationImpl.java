package com.lambda.security.web.verify.service.sms;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONObject;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.web.LambdaHttpServletRequestWrapper;
import com.lambda.security.LoginMode;
import com.lambda.security.exception.VerifyCodeValidationException;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.service.sms.model.SmsVerifyCode;
import com.lambda.security.web.verify.service.sms.store.SmsVerifyCodeStore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.AntPathMatcher;

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

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "this is thread safe")
    public SmsVerifyCodeValidationImpl(
            SecurityProperties securityProperties, SmsVerifyCodeStore<String> smsVerifyCodeStore) {
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
    public void execute(
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain)
            throws ServletException, IOException {
        LambdaHttpServletRequestWrapper httpServletRequestWrapper = getRequestWrapper(httpServletRequest);
        JSONObject requestParam = getRequestParam(httpServletRequestWrapper);
        if (MapUtils.isEmpty(requestParam)) {
            chain.doFilter(httpServletRequestWrapper, httpServletResponse);
            return;
        }

        String loginMode = requestParam.getStr(loginModeParameter);

        if (StrUtil.isEmpty(loginMode)) {
            throw new VerifyCodeValidationException("loginMode is not blank!");
        }

        LoginMode mode = LoginMode.get(loginMode);

        if (!LoginMode.SMS.equals(mode)) {
            chain.doFilter(httpServletRequestWrapper, httpServletResponse);
            return;
        }

        String mobile = requestParam.getStr(securityProperties.getSms().getMobile());
        if (StringUtils.isBlank(mobile)) {
            throw new VerifyCodeValidationException("手机号不能为空");
        }
        String code = requestParam.getStr(securityProperties.getSms().getCode());
        if (StringUtils.isBlank(code)) {
            throw new VerifyCodeValidationException("验证码不能为空");
        }

        SmsVerifyCode<String> verifyCode = smsVerifyCodeStore.get(mobile);

        if (verifyCode == null) {
            throw new VerifyCodeValidationException("验证码不存在");
        }

        boolean verified = smsVerifyCodeStore.verify(mobile, code);
        if (!verified) {
            throw new VerifyCodeValidationException("验证码不正确");
        }
        chain.doFilter(httpServletRequestWrapper, httpServletResponse);
    }
}
