package com.jingfang.security.web.verify.store;

import com.jingfang.cloud.redis.utils.RedisUtils;
import com.jingfang.security.exception.VerifyCodeExpireException;

import java.util.concurrent.TimeUnit;

/**
 * RedisCaptchaStore
 *
 * @author jpjoo
 */
public class RedisCaptchaStore implements CaptchaStore {

    private static final String REDIS_CAPTCHA_STORE_KEY = "RedisCaptchaStore:";

    @Override
    public void store(String token, String verifyCode, TimeUnit timeUnit, Integer time) {
        RedisUtils.me().setEx(REDIS_CAPTCHA_STORE_KEY + token, verifyCode, time, timeUnit);
    }

    @Override
    public boolean validate(String token, String inputCode) {
        String verifyCode = (String) RedisUtils.me().get(REDIS_CAPTCHA_STORE_KEY + token);
        if (verifyCode == null) {
            throw new VerifyCodeExpireException("验证码已过期!");
        }
        boolean matched = verifyCode.equalsIgnoreCase(inputCode);
        if (matched) {
            RedisUtils.me().delete(REDIS_CAPTCHA_STORE_KEY + token);
        }
        return matched;
    }

}
