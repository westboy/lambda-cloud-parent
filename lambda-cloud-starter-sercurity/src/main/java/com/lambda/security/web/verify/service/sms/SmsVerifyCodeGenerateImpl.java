package com.lambda.security.web.verify.service.sms;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.exception.model.ErrorModel;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.sms.SmsMessageSender;
import com.lambda.cloud.sms.model.SmsSendResult;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.service.sms.store.SmsVerifyCodeStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.Map;

/**
 * 图形校验码生成过滤器
 *
 * @author jpjoo
 */
@Slf4j
public class SmsVerifyCodeGenerateImpl implements VerifyCodeService {
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;
    private final SmsVerifyCodeStore<String> smsVerifyCodeStore;

    public SmsVerifyCodeGenerateImpl(SecurityProperties securityProperties, ObjectMapper objectMapper, SmsVerifyCodeStore<String> smsVerifyCodeStore) {
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
        this.smsVerifyCodeStore = smsVerifyCodeStore;
    }

    @Setter
    private UserDetailService userDetailService;
    @Setter
    private SmsMessageSender smsMessageSender;


    @Override
    public boolean support(HttpServletRequest request) {
        final SecurityProperties.SmsLogin smsLogin = securityProperties.getSmsLogin();
        boolean captchaEnabled = smsLogin.isEnabled();
        boolean isGetMethod = JakartaServletUtil.isGetMethod(request);
        return captchaEnabled && isGetMethod && matcher.match(smsLogin.getVerifyPath(), request.getRequestURI());
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
        this.sendVerifyCode(request, response);
    }


    public void sendVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            SecurityProperties.SmsLogin smsLogin = securityProperties.getSmsLogin();
            String mobile = request.getParameter(smsLogin.getMobile());
            Assert.isTrue(StringUtils.isNotBlank(mobile), "the parameter mobile can't be empty");
            String loginTypeParameter = "loginType";
            String loginType = request.getParameter(loginTypeParameter);
            LoginUser loginUser = userDetailService.loginByMobile(mobile, loginType);
            Assert.isTrue(null != loginUser && !loginUser.getAccountExpired() && !loginUser.getAccountLocked(), "the account is not available");
            SmsVerifyCode<String> verify = smsVerifyCodeStore.get(mobile);
            Assert.isTrue(candSend(verify), "sms code request repeatedly");
            String code = smsVerifyCodeStore.generate(mobile);
            SmsSendResult smsSendResult = smsMessageSender.sendVerifyCode(mobile, code, smsLogin.getValidMinutes());
            Map<String, Object> result = Maps.newLinkedHashMapWithExpectedSize(5);
            result.put("id", smsSendResult.getId());
            result.put("resendSeconds", smsLogin.getResendSeconds());
            result.put("validMinutes", smsLogin.getValidMinutes());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), result);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorModel model = new ErrorModel();
            model.setStatus(response.getStatus());
            model.setError(HttpStatus.EXPECTATION_FAILED.getReasonPhrase());
            model.setMessage(ex.getMessage());
            model.setPath(request.getRequestURI());
            model.setTimestamp(System.currentTimeMillis());
            objectMapper.writeValue(response.getWriter(), model);
        }
    }

    private boolean candSend(SmsVerifyCode<String> code) {
        if (code == null) {
            return true;
        }
        int sumSeconds = securityProperties.getSmsLogin().getResendSeconds() * ONE_SECOND;
        return code.getCreateTimeMillis() + sumSeconds < System.currentTimeMillis();
    }
}
