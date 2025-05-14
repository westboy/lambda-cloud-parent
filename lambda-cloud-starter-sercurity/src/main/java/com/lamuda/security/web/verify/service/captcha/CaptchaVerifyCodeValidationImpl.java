package com.lambda.security.web.verify.service.captcha;


import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONObject;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.cloud.web.DefaultServletRequestWrapper;
import com.lambda.security.exception.VerifyCodeValidationException;
import com.lambda.security.enums.LoginMode;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.store.CaptchaStore;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.AntPathMatcher;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 图形验证码校验过滤器
 *
 * @author jpjoo
 */
public class CaptchaVerifyCodeValidationImpl implements VerifyCodeService {
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final SecurityProperties securityProperties;

    private final CaptchaStore captchaStore;

    public CaptchaVerifyCodeValidationImpl(SecurityProperties securityProperties, CaptchaStore captchaStore) {
        this.securityProperties = securityProperties;
        this.captchaStore = captchaStore;
    }


    @Override
    public boolean support(HttpServletRequest request) {
        final SecurityProperties.Verify verify = securityProperties.getForm().getVerify();
        boolean captchaEnabled = verify.isEnabled();
        boolean isPostMethod = JakartaServletUtil.isPostMethod(request);
        return captchaEnabled && isPostMethod && matcher.match(securityProperties.getForm().getLoginProcessingUrl(), request.getRequestURI());
    }

    @Override
    public void execute(HttpServletRequest httpServletRequest, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        DefaultServletRequestWrapper request = new DefaultServletRequestWrapper(httpServletRequest);
        Map<String, Object> formRequest = WebHttpUtils.getFormRequest(request);
        JSONObject ajaxRequest = (JSONObject) WebHttpUtils.getRequestBody(request);
        ajaxRequest.putAll(formRequest);
        if (MapUtils.isEmpty(ajaxRequest)) {
            chain.doFilter(request, response);
            return;
        }
        String loginMode = ajaxRequest.getStr("loginMode");
        if (StrUtil.isEmpty(loginMode)) {
            throw new VerifyCodeValidationException("登录模式不能为空!");
        }
        boolean isPwdLogin = LoginMode.PWD.getCode().equals(loginMode);
        if (!isPwdLogin) {
            chain.doFilter(request, response);
            return;
        }
        String verifyCode = obtainVerifyCode(ajaxRequest);
        String verifyToken = obtainVerifyToken(ajaxRequest);
        if (StringUtils.isBlank(verifyCode)) {
            throw new VerifyCodeValidationException("验证码不能为空!");
        }
        if (StringUtils.isBlank(verifyToken)) {
            throw new VerifyCodeValidationException("__TOKEN不能为空!");
        }
        boolean verified = captchaStore.validate(verifyToken, verifyCode);
        if (!verified) {
            throw new VerifyCodeValidationException("验证码不正确!");
        }
        chain.doFilter(request, response);
    }

    private String obtainVerifyToken(Map<String, Object> map) {
        Object token = map.getOrDefault(CaptchaVerifyCodeGenerateImpl.TOKEN_KEY, "");
        return String.valueOf(token);
    }

    private String obtainVerifyCode(Map<String, Object> map) {
        Object verify = map.getOrDefault(CaptchaVerifyCodeGenerateImpl.VERIFY_CODE_PARAMETER, "");
        return String.valueOf(verify);
    }
}
