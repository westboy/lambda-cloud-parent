package com.lambda.security.web.verify.service.sms.store;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.lambda.cloud.core.utils.StringUtils;
import com.lambda.cloud.redis.utils.RedisUtils;
import com.lambda.security.web.verify.service.sms.SmsVerifyCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class InRedisSmsVerifyCodeStore implements SmsVerifyCodeStore<String> {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String COMMON = "Authorization:login:smsVerify";

    public InRedisSmsVerifyCodeStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String generate(String key) {
        String code = Integer.toString(RandomUtil.randomInt(1000, 9999));
        String json = JSONUtil.toJsonStr(new SmsVerifyCode<>(code));
        stringRedisTemplate.opsForValue().set(getCommonKey(key),json, getPeriod(), TimeUnit.SECONDS);
        return code;
    }

    @Override
    public boolean verify(@NonNull String key, @NonNull String code) {
        String redisCode = stringRedisTemplate.opsForValue().get(getCommonKey(key));
        if (StringUtils.isEmpty(redisCode)) {
            return false;
        }
        SmsVerifyCode redisCodeObj = JSONUtil.toBean(redisCode, SmsVerifyCode.class);
        boolean result = code.equals(redisCodeObj.getCode());
        if (result) {
            stringRedisTemplate.delete(getCommonKey(key));
        }
        return result;
    }

    @Override
    public SmsVerifyCode<String> get(String key) {
        String redisCode = stringRedisTemplate.opsForValue().get(getCommonKey(key));
        if (StringUtils.isEmpty(redisCode)) {
            return new SmsVerifyCode<>(null);
        }
        return JSONUtil.toBean(redisCode, SmsVerifyCode.class);
    }

    @Override
    public boolean verifyReSend(String key) {
        String redisCode = stringRedisTemplate.opsForValue().get(getCommonKey(key));
        if (StringUtils.isEmpty(redisCode)) {
            return false;
        }
        SmsVerifyCode redisCodeObj = JSONUtil.toBean(redisCode, SmsVerifyCode.class);
        LocalDateTime created = LocalDateTimeUtil.of(redisCodeObj.getCreateTimeMillis());
        return created.plusSeconds(getInterval()).isBefore(LocalDateTime.now());
    }

    private String getCommonKey(String key) {
        return MessageFormat.format("{0}:{1}", COMMON, key);
    }
}
