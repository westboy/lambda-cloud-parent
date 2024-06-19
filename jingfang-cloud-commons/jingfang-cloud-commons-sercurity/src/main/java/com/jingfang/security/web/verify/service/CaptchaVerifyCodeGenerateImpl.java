package com.jingfang.security.web.verify.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.servlet.ServletUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.jingfang.autoconfig.SecurityProperties;
import com.jingfang.cloud.mvc.WebHttpUtils;
import com.jingfang.security.web.verify.store.CaptchaStore;
import org.apache.commons.codec.binary.Base64;
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
public class CaptchaVerifyCodeGenerateImpl implements VerifyCodeService {

    private final AntPathMatcher matcher = new AntPathMatcher();

    private final SecurityProperties securityProperties;

    private final ObjectMapper objectMapper;

    private final CaptchaStore captchaStore;

    public static final String TOKEN_KEY = "__token";

    public static final String VERIFY_CODE_PARAMETER = "verify";

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
        byte[] captchaChallengeAsJpeg;
        CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(200,150,4, 3);
        String captchaId = IdUtil.fastUUID();
        String captchaCode = captcha.getCode();
        captchaStore.store(captchaId,captchaCode, TimeUnit.SECONDS,60);
        try (ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(captcha.getImage(), "JPEG", jpegOutputStream);
            captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
            if (ajax) {
                try (PrintWriter writer = response.getWriter()) {
                    response.setHeader("Expires", "0");
                    response.setHeader("Pragma", "No-cache");
                    response.setHeader("Cache-Control", "no-cache");
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    Map<String, String> result = Maps.newHashMapWithExpectedSize(2);
                    result.put(TOKEN_KEY, captchaId);
                    result.put(VERIFY_CODE_PARAMETER, Base64.encodeBase64String(captchaChallengeAsJpeg));
                    objectMapper.writeValue(writer, result);
                }
            } else {
                try (ServletOutputStream output = response.getOutputStream()) {
                    output.write(captchaChallengeAsJpeg);
                }
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
