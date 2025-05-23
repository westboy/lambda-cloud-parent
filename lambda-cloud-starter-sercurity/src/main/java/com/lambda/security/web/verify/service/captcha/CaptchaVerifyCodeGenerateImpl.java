package com.lambda.security.web.verify.service.captcha;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.security.exception.VerifyCodeValidationException;
import com.lambda.security.web.verify.generator.MathGenerator;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.service.captcha.store.CaptchaStore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;

/**
 * 图形校验码生成过滤器
 *
 * @author jpjoo
 */
@Slf4j
public class CaptchaVerifyCodeGenerateImpl implements VerifyCodeService {

    private final AntPathMatcher matcher = new AntPathMatcher();

    private final SecurityProperties securityProperties;

    private final ObjectMapper objectMapper;

    private final CaptchaStore captchaStore;

    public static final String TOKEN_KEY = "__token";

    public static final String VERIFY_CODE_PARAMETER = "verifyCode";

    public static final String VERIFY_IMAGE = "verifyImage";

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "RedisHelper is thread safe")
    public CaptchaVerifyCodeGenerateImpl(
            SecurityProperties securityProperties, ObjectMapper objectMapper, CaptchaStore captchaStore) {
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
        this.captchaStore = captchaStore;
    }

    @Override
    public boolean support(HttpServletRequest request) {
        final SecurityProperties.Verify verify = securityProperties.getVerify();
        boolean captchaEnabled = securityProperties.getForm().isEnableVerify();
        boolean isGetMethod = JakartaServletUtil.isGetMethod(request);
        return captchaEnabled && isGetMethod && matcher.match(verify.getUrl(), request.getRequestURI());
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException {
        this.writeCaptcha(request, response);
    }

    public void writeCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(
                securityProperties.getVerify().getCaptchaWidth(),
                securityProperties.getVerify().getCaptchaHeight(),
                securityProperties.getVerify().getCaptchaCodeCount(),
                3);
        MathGenerator mathGenerator =
                new MathGenerator(securityProperties.getVerify().getCaptchaNumberLength());
        captcha.setGenerator(mathGenerator);
        String captchaId = IdUtil.fastUUID();
        Integer captchaCode = (int) Calculator.conversion(captcha.getCode());
        if (securityProperties.getVerify().isDevMode()) {
            log.info("验证码[ {}:{}, {}:{} ]", TOKEN_KEY, captchaId, VERIFY_CODE_PARAMETER, captchaCode);
        }
        captchaStore.store(
                captchaId,
                captchaCode.toString(),
                securityProperties.getVerify().getTimeUnit(),
                securityProperties.getVerify().getDuration());
        if (WebHttpUtils.isAjaxRequest(request)) {
            try (PrintWriter writer = response.getWriter()) {
                response.setHeader("Expires", "0");
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                Map<String, String> result = Maps.newHashMapWithExpectedSize(2);
                result.put(TOKEN_KEY, captchaId);
                result.put(VERIFY_IMAGE, captcha.getImageBase64Data());
                objectMapper.writeValue(writer, result);
            } catch (Exception e) {
                throw new VerifyCodeValidationException(e.getMessage());
            }
        } else {
            // 这个只作为测试使用
            try (ServletOutputStream output = response.getOutputStream()) {
                captcha.write(output);
            }
        }
    }
}
