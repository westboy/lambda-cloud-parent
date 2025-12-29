package com.lambda.security.web.form.validator;

import com.lambda.autoconfig.SecurityProperties;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.exception.CaptchaRequiredException;
import com.lambda.security.exception.VerifyCodeValidationException;
import com.lambda.security.web.form.CaptchaTriggerStrategy;
import com.lambda.security.web.form.FormLoginContext;
import com.lambda.security.web.form.FormLoginValidator;
import com.lambda.security.web.verify.service.captcha.CaptchaVerifyCodeGenerateImpl;
import com.lambda.security.web.verify.service.captcha.store.CaptchaStore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 动态验证码验证器
 *
 * <p>实现 FormLoginValidator 接口，在表单登录验证流程中
 * 根据用户的失败次数动态判断是否需要验证码验证。</p>
 *
 * <h3>验证逻辑</h3>
 * <ol>
 *   <li>检查是否启用动态验证码触发</li>
 *   <li>检查当前用户的失败次数是否达到触发阈值</li>
 *   <li>如果需要验证码，则验证用户提交的验证码</li>
 *   <li>验证失败抛出异常，成功则继续登录流程</li>
 * </ol>
 *
 * @author jpjoo
 * @see FormLoginValidator
 * @see CaptchaTriggerStrategy
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
@Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级，在其他验证之前执行
public class DynamicCaptchaValidator implements FormLoginValidator {

    private final CaptchaTriggerStrategy captchaTriggerStrategy;
    private final CaptchaStore captchaStore;
    private final SecurityProperties securityProperties;

    public DynamicCaptchaValidator(
            CaptchaTriggerStrategy captchaTriggerStrategy,
            CaptchaStore captchaStore,
            SecurityProperties securityProperties) {
        this.captchaTriggerStrategy = captchaTriggerStrategy;
        this.captchaStore = captchaStore;
        this.securityProperties = securityProperties;
    }

    @Override
    public boolean support(FormLoginContext context) {
        // 仅在启用动态验证码触发且登录模式为密码模式时生效
        if (!securityProperties.getForm().getCaptchaTrigger().isEnabled()) {
            return false;
        }

        // 如果全局强制验证码已启用，则跳过动态触发
        // （由 CaptchaVerifyCodeValidationImpl 处理）
        if (securityProperties.getForm().isEnableVerify()) {
            return false;
        }

        String username = context.username();
        return StringUtils.isNotBlank(username);
    }

    @Override
    public void validate(FormLoginContext context) throws AuthenticationException {
        String username = context.username();

        // 检查是否需要验证码
        if (!captchaTriggerStrategy.isCaptchaRequired(username)) {
            return; // 不需要验证码，直接通过
        }

        // 需要验证码，从请求中获取验证码参数
        Map<String, Object> requestBody = context.requestBody();

        String verifyCode = getVerifyCode(requestBody, context.request());
        String verifyToken = getVerifyToken(requestBody, context.request());

        if (StringUtils.isBlank(verifyCode)) {
            int failureTimes = captchaTriggerStrategy.getFailureTimes(username);
            throw new CaptchaRequiredException(
                    "登录失败次数过多，请输入验证码", failureTimes, captchaTriggerStrategy.getTriggerTimes());
        }

        if (StringUtils.isBlank(verifyToken)) {
            throw new VerifyCodeValidationException("验证码令牌不能为空");
        }

        // 验证验证码
        boolean verified = captchaStore.validate(verifyToken, verifyCode);
        if (!verified) {
            throw new VerifyCodeValidationException("验证码不正确");
        }
    }

    private String getVerifyCode(Map<String, Object> requestBody, HttpServletRequest request) {
        if (MapUtils.isNotEmpty(requestBody)) {
            Object code = requestBody.get(CaptchaVerifyCodeGenerateImpl.VERIFY_CODE_PARAMETER);
            if (code != null) {
                return String.valueOf(code);
            }
        }
        return request.getParameter(CaptchaVerifyCodeGenerateImpl.VERIFY_CODE_PARAMETER);
    }

    private String getVerifyToken(Map<String, Object> requestBody, HttpServletRequest request) {
        if (MapUtils.isNotEmpty(requestBody)) {
            Object token = requestBody.get(CaptchaVerifyCodeGenerateImpl.TOKEN_KEY);
            if (token != null) {
                return String.valueOf(token);
            }
        }
        return request.getParameter(CaptchaVerifyCodeGenerateImpl.TOKEN_KEY);
    }
}
