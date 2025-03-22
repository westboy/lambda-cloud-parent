package com.lamuda.security.web.authentication.locking;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * RedisLockingStrategy
 *
 * @author jpjoo
 */
@Setter
public class RedisLockingStrategy extends AbstractLockingStrategy {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String COMMON = "JFC:USER:LOGINFAILURE:";

    public RedisLockingStrategy(int maxFailureTimes, int duration, TimeUnit timeUnit, StringRedisTemplate stringRedisTemplate) {
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
        return this.getFailureTimes(username) >= getMaxFailureTimes();
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
        int failCount;
        try {
            failCount = Integer.parseInt(Objects.requireNonNull(stringRedisTemplate.opsForValue().get(getCommonKey(username))));
        } catch (NullPointerException e) {
            failCount = 0;
        }
        return failCount;
    }

    private Long getLockedTime(String username) {
        Long lockedTimes;
        try {
            lockedTimes = Objects.requireNonNull(stringRedisTemplate.opsForValue().getOperations().getExpire(getCommonKey(username), TimeUnit.MILLISECONDS));
        } catch (NullPointerException e) {
            lockedTimes = null;
        }
        return lockedTimes;
    }

    private Long saveFailureTimes(String username, int times) {
        if (this.checkFailureTimes(username)) {
            return System.currentTimeMillis() - (this.getTimeUnit().toMillis(super.getDuration()) - this.getLockedTime(username));
        } else {
            stringRedisTemplate.opsForValue().set(getCommonKey(username), String.valueOf(times), super.getDuration(), super.getTimeUnit());
            return System.currentTimeMillis();
        }
    }

    private String getCommonKey(String username) {
        return MessageFormat.format("{0}:{1}", COMMON, username);
    }
}
