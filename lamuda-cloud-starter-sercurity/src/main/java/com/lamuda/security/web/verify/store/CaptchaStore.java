package com.lamuda.security.web.verify.store;

import java.util.concurrent.TimeUnit;

/**
 * CaptchaStore
 *
 * @author jpjoo
 */
public interface CaptchaStore {

    /**
     * 持久化验证码
     *
     * @param token
     * @param verifyCode
     * @param timeUnit
     * @param time
     */
    void store(String token, String verifyCode, TimeUnit timeUnit, Integer time);

    /**
     * 验证方法
     *
     * @param token
     * @param inputCode
     * @return
     */
    boolean validate(String token, String inputCode);

}
