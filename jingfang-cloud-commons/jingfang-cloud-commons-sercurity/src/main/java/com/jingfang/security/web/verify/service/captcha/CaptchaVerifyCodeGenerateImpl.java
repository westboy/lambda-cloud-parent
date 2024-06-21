package com.jingfang.security.web.verify.service.captcha;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.jingfang.autoconfig.SecurityProperties;
import com.jingfang.cloud.mvc.WebHttpUtils;
import com.jingfang.security.exception.VerifyCodeValidationException;
import com.jingfang.security.web.verify.service.VerifyCodeService;
import com.jingfang.security.web.verify.store.CaptchaStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;

import javax.imageio.ImageIO;
import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    public CaptchaVerifyCodeGenerateImpl(SecurityProperties securityProperties, ObjectMapper objectMapper, CaptchaStore captchaStore) {
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
        this.captchaStore = captchaStore;
    }


    @Override
    public boolean support(HttpServletRequest request) {
        final SecurityProperties.Verify verify = securityProperties.getForm().getVerify();
        boolean captchaEnabled = verify.isEnabled();
        boolean isGetMethod = ServletUtil.isGetMethod(request);
        return captchaEnabled && isGetMethod && matcher.match(verify.getUrl(), request.getRequestURI());
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException {
        this.writeCaptcha(request, response);
    }


    public void writeCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean ajax = WebHttpUtils.isAjaxRequest(request);
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(200, 150, 4, 3);
        String captchaId = IdUtil.fastUUID();
        String captchaCode = captcha.getCode();
        if (securityProperties.getForm().getVerify().isDevMode()) {
            log.info("验证码[ {}:{}, {}:{} ]", TOKEN_KEY, captchaId, VERIFY_CODE_PARAMETER, captchaCode);
        }
        captchaStore.store(captchaId, captchaCode, TimeUnit.SECONDS, 60);
        if (ajax) {
            try (PrintWriter writer = response.getWriter()) {
                response.setHeader("Expires", "0");
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                Map<String, String> result = Maps.newHashMapWithExpectedSize(2);
                result.put(TOKEN_KEY, captchaId);
                result.put(VERIFY_CODE_PARAMETER, captcha.getImageBase64());
                objectMapper.writeValue(writer, result);
            } catch (Exception e) {
                throw new VerifyCodeValidationException(e.getMessage());
            }
        } else {
            try (ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
                 ServletOutputStream output = response.getOutputStream()) {
                ImageIO.write(captcha.getImage(), "JPEG", jpegOutputStream);
                byte[] captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
                output.write(captchaChallengeAsJpeg);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
