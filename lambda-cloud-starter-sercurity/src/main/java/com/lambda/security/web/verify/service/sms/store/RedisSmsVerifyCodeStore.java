package com.lambda.security.web.verify.service.sms.store;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lambda.cloud.core.utils.StringUtils;
import com.lambda.security.exception.VerifyCodeValidationException;
import com.lambda.security.web.verify.service.sms.SmsVerifyCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class RedisSmsVerifyCodeStore implements SmsVerifyCodeStore<String> {
    private final Gson gson = new Gson();
    private static final String PREFIX_KEY = "Authorization:login:smsVerify";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisSmsVerifyCodeStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String generate(String key) {
        String code = Integer.toString(RandomUtil.randomInt(1000, 9999));
        String json = JSONUtil.toJsonStr(new SmsVerifyCode<>(code));
        stringRedisTemplate.opsForValue().set(getCommonKey(key), json, getPeriod(), TimeUnit.SECONDS);
        return code;
    }

    @Override
    public boolean verify(@NonNull String key, @NonNull String code) {
        SmsVerifyCode<String> smsVerifyCode = get(key);
        boolean result = code.equals(smsVerifyCode.getCode());
        if (result) {
            stringRedisTemplate.delete(getCommonKey(key));
        }
        return result;
    }

    @Override
    public SmsVerifyCode<String> get(String key) {
        String cache = stringRedisTemplate.opsForValue().get(getCommonKey(key));
        if (StringUtils.isEmpty(cache)) {
            throw new VerifyCodeValidationException("code is expired");
        }
        return gson.fromJson(cache, new SmsVerifyCodeTypeToken().getType());
    }

    @Override
    public boolean verifyReSend(String key) {
        SmsVerifyCode<String> smsVerifyCode = get(key);
        LocalDateTime created = LocalDateTimeUtil.of(smsVerifyCode.getCreateTimeMillis());
        return created.plusSeconds(getInterval()).isBefore(LocalDateTime.now());
    }

    private String getCommonKey(String key) {
        return MessageFormat.format("{0}:{1}", PREFIX_KEY, key);
    }

    private static class SmsVerifyCodeTypeToken extends TypeToken<SmsVerifyCode<String>> {
    }
}
