package com.lambda.security.web.form.locking;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * RedisLockingStrategy
 *
 * @author jpjoo
 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP2"},
        justification = "springboot properties")
@Setter
public class RedisLockingStrategy extends AbstractLockingStrategy {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String COMMON = "LAMBDA-CLOUD:USER:LOGIN_FAILURE:";

    public RedisLockingStrategy(
            int maxFailureTimes, int duration, TimeUnit timeUnit, StringRedisTemplate stringRedisTemplate) {
        super(maxFailureTimes, duration, timeUnit);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /***
     * 登陆失败缓存处理
     * @param username 用户账号
     */
    @Override
    public UserLoginLimitTracker doLoginFailure(String username) {
        int times = this.getFailureTimes(username);
        Long lockedTime = this.saveFailureTimes(username, ++times);
        return new UserLoginLimitTracker(getMaxFailureTimes(), times, lockedTime);
    }

    /***
     * 登陆成功缓存处理
     * @param username 用户账号
     * @return
     */
    @Override
    public void loginSuccess(String username) {
        stringRedisTemplate.delete(getCommonKey(username));
    }

    /**
     * 验证用户登陆次数是否超额
     *
     * @param username 用户账号
     * @return int
     */
    @Override
    public boolean doCheckFailureTimes(String username) {
        if (0 == this.getMaxFailureTimes()) {
            return false;
        }
        return this.getFailureTimes(username) >= this.getMaxFailureTimes();
    }

    @Override
    public void unlock(String username) {
        stringRedisTemplate.delete(getCommonKey(username));
    }

    @Override
    public boolean getLockedState(String username) {
        return checkFailureTimes(username);
    }

    private int getFailureTimes(String username) {
        String failCount = stringRedisTemplate.opsForValue().get(getCommonKey(username));
        if (Objects.isNull(failCount)) {
            return 0;
        } else {
            return Integer.parseInt(failCount);
        }
    }

    private Long getLockedTime(String username) {
        Long expire = stringRedisTemplate
                .opsForValue()
                .getOperations()
                .getExpire(getCommonKey(username), TimeUnit.MILLISECONDS);
        if (expire == null) {
            return 0L;
        }
        return expire;
    }

    private Long saveFailureTimes(String username, int times) {
        if (this.checkFailureTimes(username)) {
            long diff = this.getTimeUnit().toMillis(super.getDuration()) - this.getLockedTime(username);
            return System.currentTimeMillis() - diff;
        } else {
            stringRedisTemplate
                    .opsForValue()
                    .set(getCommonKey(username), String.valueOf(times), super.getDuration(), super.getTimeUnit());
            return System.currentTimeMillis();
        }
    }

    private String getCommonKey(String username) {
        return MessageFormat.format("{0}:{1}", COMMON, username);
    }
}
