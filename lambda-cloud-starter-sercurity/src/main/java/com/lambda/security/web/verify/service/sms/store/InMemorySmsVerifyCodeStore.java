package com.lambda.security.web.verify.service.sms.store;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import com.lambda.security.web.verify.service.sms.SmsVerifyCode;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DefaultSmsVerifyCodeStore
 *
 * @author Jin
 **/
public class InMemorySmsVerifyCodeStore implements SmsVerifyCodeStore<String> {
    private final Map<String, SmsVerifyCode<String>> map = new ConcurrentHashMap<>();

    @Override
    public String generate(String key) {
        String code = Integer.toString(RandomUtil.randomInt(1000, 9999));
        map.put(key, new SmsVerifyCode<>(code));
        return code;
    }

    @Override
    public boolean verify(@NonNull String key, @NonNull String code) {
        SmsVerifyCode<String> verify = map.get(key);
        if (null == verify) {
            return false;
        }
        boolean result = verify.getCode().equals(code);
        if (result) {
            map.remove(key);
        }
        return result;
    }

    @Override
    public SmsVerifyCode<String> get(String key) {
        return map.get(key);
    }

    @Override
    public boolean verifyReSend(String key) {
        SmsVerifyCode<String> code = get(key);
        if (code == null) {
            return true;
        }
        LocalDateTime created = LocalDateTimeUtil.of(code.getCreateTimeMillis());
        return created.plusSeconds(getInterval()).isBefore(LocalDateTime.now());
    }

}
