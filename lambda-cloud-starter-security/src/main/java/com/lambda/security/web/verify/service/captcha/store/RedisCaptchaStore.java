package com.lambda.security.web.verify.service.captcha.store;

import com.lambda.cloud.redis.helper.RedisHelper;
import com.lambda.security.exception.VerifyCodeExpireException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.TimeUnit;
import lombok.Setter;

/**
 * RedisCaptchaStore
 *
 * @author jpjoo
 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP2"},
        justification = "springboot properties")
@Setter
public class RedisCaptchaStore implements CaptchaStore {

    private static final String REDIS_CAPTCHA_STORE_KEY = "RedisCaptchaStore:";
    private RedisHelper redisHelper;

    @Override
    public void store(String token, String verifyCode, TimeUnit timeUnit, Integer time) {
        redisHelper.setEx(REDIS_CAPTCHA_STORE_KEY + token, verifyCode, time, timeUnit);
    }

    @Override
    public boolean validate(String token, String inputCode) {
        String verifyCode = (String) redisHelper.get(REDIS_CAPTCHA_STORE_KEY + token);
        if (verifyCode == null) {
            throw new VerifyCodeExpireException("验证码已过期!");
        }
        boolean matched = verifyCode.equalsIgnoreCase(inputCode);
        if (matched) {
            redisHelper.delete(REDIS_CAPTCHA_STORE_KEY + token);
        }
        return matched;
    }
}
