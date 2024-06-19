package com.jingfang.security.web.verify.store;

import java.util.concurrent.TimeUnit;

public class RedisCaptchaStore implements CaptchaStore {

    @Override
    public void store(String token, String verifyCode, TimeUnit timeUnit, Integer time) {

    }

    @Override
    public boolean validate(String token, String inputCode) {
        return false;
    }

}
