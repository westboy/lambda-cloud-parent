package com.lambda.security.web.verify.service.sms;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.exception.model.ErrorModel;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.cloud.sms.SmsMessageSender;
import com.lambda.cloud.sms.model.SmsSendResult;
import com.lambda.cloud.web.LambdaHttpServletRequestWrapper;
import com.lambda.security.LoginCode;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.exception.VerifyCodeValidationException;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.service.captcha.CaptchaVerifyCodeGenerateImpl;
import com.lambda.security.web.verify.service.captcha.store.CaptchaStore;
import com.lambda.security.web.verify.service.sms.model.SmsVerifyCodeResponse;
import com.lambda.security.web.verify.service.sms.store.SmsVerifyCodeStore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;

/**
 * 图形校验码生成过滤器
 *
 * @author jpjoo
 */
@SuppressFBWarnings(value = "REC_CATCH_EXCEPTION")
@Slf4j
public class SmsVerifyCodeGenerateImpl implements VerifyCodeService {
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;
    private final SmsVerifyCodeStore<String> smsVerifyCodeStore;
    private final SecurityProperties.SmsLogin smsLogin;

    @Setter
    private String loginTypeParameter = "loginType";

    @Setter
    private CaptchaStore captchaStore;

    @Setter
    private UserDetailService userDetailService;

    @Setter
    private SmsMessageSender smsMessageSender;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "this is thread safe")
    public SmsVerifyCodeGenerateImpl(
            SecurityProperties securityProperties,
            ObjectMapper objectMapper,
            SmsVerifyCodeStore<String> smsVerifyCodeStore) {
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
        this.smsVerifyCodeStore = smsVerifyCodeStore;
        this.smsLogin = securityProperties.getSms();
    }

    @Override
    public boolean support(HttpServletRequest request) {
        final SecurityProperties.SmsLogin smsLogin = securityProperties.getSms();
        return smsLogin.isEnabled()
                && JakartaServletUtil.isPostMethod(request)
                && matcher.match(smsLogin.getVerifyPath(), request.getRequestURI());
    }

    @Override
    public void execute(
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain)
            throws IOException {
        try {
            LambdaHttpServletRequestWrapper httpServletRequestWrapper = getRequestWrapper(httpServletRequest);
            JSONObject requestParam = getRequestParam(httpServletRequestWrapper);

            if (MapUtils.isEmpty(requestParam)) {
                chain.doFilter(httpServletRequestWrapper, httpServletResponse);
                return;
            }

            String mobile = requestParam.getStr(securityProperties.getSms().getMobile());

            if (mobile == null) {
                throw new VerifyCodeValidationException("the mobile is not exist");
            }

            String loginType = requestParam.getStr(loginTypeParameter);

            boolean containsLoginType = StpLogicUtils.containsLoginType(loginType);
            if (!containsLoginType) {
                throw new AuthenticationException(LoginCode.CODE_20000, "登录类型错误！");
            }

            LoginUser loginUser = userDetailService.loginByMobile(mobile, loginType);

            if (loginUser == null) {
                throw new VerifyCodeValidationException("账号密码错误");
            }

            if (loginUser.getAccountExpired()) {
                throw new VerifyCodeValidationException("账号已过期");
            }

            if (loginUser.getAccountLocked()) {
                throw new VerifyCodeValidationException("账号已锁定");
            }

            if (!smsVerifyCodeStore.verifyReSend(mobile)) {
                throw new VerifyCodeValidationException("the sms code request repeatedly");
            }

            if (smsLogin.isEnableVerify()) {

                String verifyToken = requestParam.getStr(CaptchaVerifyCodeGenerateImpl.TOKEN_KEY);
                Assert.isBlank(verifyToken, "__TOKEN不能为空!");

                String verifyCode = requestParam.getStr(CaptchaVerifyCodeGenerateImpl.VERIFY_CODE_PARAMETER);
                Assert.isBlank(verifyCode, "验证码不能为空!");

                boolean verified = captchaStore.validate(verifyToken, verifyCode);
                if (!verified) {
                    throw new VerifyCodeValidationException("验证码不正确!");
                }
            }

            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String code = smsVerifyCodeStore.generate(mobile);
            SmsSendResult smsSendResult = smsMessageSender.sendVerifyCode(mobile, code, smsLogin.getValidMinutes());
            if (smsSendResult == null || !smsSendResult.isSuccess()) {
                throw new VerifyCodeValidationException("the sms code send failed");
            }
            SmsVerifyCodeResponse smsVerifyCodeResponse = new SmsVerifyCodeResponse();
            smsVerifyCodeResponse.setId(smsSendResult.getId());
            smsVerifyCodeResponse.setResendSeconds(smsLogin.getResendSeconds());
            smsVerifyCodeResponse.setValidMinutes(smsLogin.getValidMinutes());
            if (smsLogin.isMock()) {
                smsVerifyCodeResponse.setMessage(smsSendResult.getMessage());
            }
            objectMapper.writeValue(httpServletResponse.getWriter(), smsVerifyCodeResponse);
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorModel model = new ErrorModel();
            model.setStatus(httpServletResponse.getStatus());
            model.setError(HttpStatus.EXPECTATION_FAILED.getReasonPhrase());
            model.setMessage(ex.getMessage());
            model.setPath(httpServletRequest.getRequestURI());
            model.setTimestamp(System.currentTimeMillis());
            objectMapper.writeValue(httpServletResponse.getWriter(), model);
        }
    }
}
