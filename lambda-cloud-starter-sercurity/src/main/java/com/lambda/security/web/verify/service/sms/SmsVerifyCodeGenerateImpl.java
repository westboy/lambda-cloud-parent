package com.lambda.security.web.verify.service.sms;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.exception.model.ErrorModel;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.sms.SmsMessageSender;
import com.lambda.cloud.sms.model.SmsSendResult;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.service.captcha.store.CaptchaStore;
import com.lambda.security.web.verify.service.sms.model.SmsVerifyCode;
import com.lambda.security.web.verify.service.sms.model.SmsVerifyCodeResponse;
import com.lambda.security.web.verify.service.sms.store.SmsVerifyCodeStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

/**
 * 图形校验码生成过滤器
 *
 * @author jpjoo
 */
@Slf4j
public class SmsVerifyCodeGenerateImpl implements VerifyCodeService {
    private final AntPathMatcher matcher = new AntPathMatcher();
    private static final String TOKEN_KEY = "__token";
    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;
    private final SmsVerifyCodeStore<String> smsVerifyCodeStore;

    @Setter
    private String loginTypeParameter = "loginType";
    @Setter
    private String verifyCodeParameter = "verify";
    @Setter
    private CaptchaStore captchaStore;
    @Setter
    private UserDetailService userDetailService;
    @Setter
    private SmsMessageSender smsMessageSender;

    public SmsVerifyCodeGenerateImpl(SecurityProperties securityProperties, ObjectMapper objectMapper, SmsVerifyCodeStore<String> smsVerifyCodeStore) {
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
        this.smsVerifyCodeStore = smsVerifyCodeStore;
    }


    @Override
    public boolean support(HttpServletRequest request) {
        final SecurityProperties.SmsLogin smsLogin = securityProperties.getSms();
        return smsLogin.isEnabled()
                && JakartaServletUtil.isPostMethod(request)
                && matcher.match(smsLogin.getVerifyPath(), request.getRequestURI());
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
        this.sendVerifyCode(request, response);
    }


    public void sendVerifyCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            SecurityProperties.SmsLogin smsLogin = securityProperties.getSms();
            String mobile = request.getParameter(smsLogin.getMobile());
            if (StrUtil.isBlank(mobile)) {
                throw new IllegalArgumentException("the parameter mobile can't be empty");
            }
            String loginType = request.getParameter(loginTypeParameter);
            LoginUser loginUser = userDetailService.loginByMobile(mobile, loginType);

            if (loginUser == null) {
                throw new IllegalArgumentException("the mobile is not exist");
            }

            if (loginUser.getAccountExpired() == null || !loginUser.getAccountExpired()) {
                throw new IllegalArgumentException("the account is expired");
            }

            SmsVerifyCode<String> verify = smsVerifyCodeStore.get(mobile);

            if (verify == null) {
                throw new IllegalArgumentException("the sms code request repeatedly");
            }

            if (!candSend(verify)) {
                throw new IllegalArgumentException("the sms code request repeatedly");
            }
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String code = smsVerifyCodeStore.generate(mobile);
            SmsSendResult smsSendResult = smsMessageSender.sendVerifyCode(mobile, code, smsLogin.getValidMinutes());
            SmsVerifyCodeResponse smsVerifyCodeResponse = new SmsVerifyCodeResponse();
            smsVerifyCodeResponse.setId(smsSendResult.getId());
            smsVerifyCodeResponse.setResendSeconds(smsLogin.getResendSeconds());
            smsVerifyCodeResponse.setValidMinutes(smsLogin.getValidMinutes());
            objectMapper.writeValue(response.getWriter(), smsVerifyCodeResponse);
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
        int sumSeconds = securityProperties.getSms().getResendSeconds() * ONE_SECOND;
        return code.getCreateTimeMillis() + sumSeconds < System.currentTimeMillis();
    }
}
