package com.jingfang.security.web.verify.store;

import java.util.concurrent.TimeUnit;

public interface CaptchaStore {

    void store(String token, String verifyCode, TimeUnit timeUnit,Integer time);

    boolean validate(String token, String inputCode);

}
