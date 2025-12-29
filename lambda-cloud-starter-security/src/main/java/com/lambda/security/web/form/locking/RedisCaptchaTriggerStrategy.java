package com.lambda.security.web.form.locking;

import com.lambda.security.web.form.CaptchaTriggerStrategy;
import java.text.MessageFormat;
import java.util.Objects;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于Redis的验证码动态触发策略实现
 *
 * <p>复用 FormLockingStrategy 的 Redis 存储，
 * 通过不同的阈值判断实现验证码触发功能。</p>
 *
 * <h3>工作原理</h3>
 * <ul>
 *   <li>复用 RedisLockingStrategy 的失败计数存储</li>
 *   <li>使用较低的阈值判断是否触发验证码</li>
 *   <li>与锁定策略使用相同的 TTL 周期</li>
 * </ul>
 *
 * <h3>Redis存储结构</h3>
 * <pre>
 * Key: LAMBDA-CLOUD:USER:LOGIN_FAILURE:{username}
 * Value: 失败次数
 * TTL: 共享的过期时间
 * </pre>
 *
 * @author jpjoo
 */
public class RedisCaptchaTriggerStrategy implements CaptchaTriggerStrategy {

    private static final String COMMON = "LAMBDA-CLOUD:USER:LOGIN_FAILURE:";

    private final StringRedisTemplate stringRedisTemplate;
    private final int triggerTimes;

    public RedisCaptchaTriggerStrategy(StringRedisTemplate stringRedisTemplate, int triggerTimes) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.triggerTimes = triggerTimes;
    }

    @Override
    public boolean isCaptchaRequired(String username) {
        if (triggerTimes <= 0) {
            return false;  // 0表示不启用动态触发
        }
        return getFailureTimes(username) >= triggerTimes;
    }

    @Override
    public int getFailureTimes(String username) {
        String failCount = stringRedisTemplate.opsForValue().get(getCommonKey(username));
        return failCount == null ? 0 : Integer.parseInt(failCount);
    }

    @Override
    public int getTriggerTimes() {
        return triggerTimes;
    }

    private String getCommonKey(String username) {
        return MessageFormat.format("{0}:{1}", COMMON, username);
    }
}
