package com.lambda.security.web.authentication.locking;

import com.lambda.security.web.SecurityLockingStrategy;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

/**
 * AbstractLockingStrategy
 *
 * @author jpjoo
 */
@Setter
public abstract class AbstractLockingStrategy implements SecurityLockingStrategy {

    @Getter
    private int maxFailureTimes;

    private int duration;

    private TimeUnit timeUnit;

    protected AbstractLockingStrategy(int maxFailureTimes, int duration, TimeUnit timeUnit) {
        this.maxFailureTimes = maxFailureTimes;
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Override
    public UserLoginLimitTracker loginFailure(String username) {
        //0不限制登陆失败次数
        if (0 == this.getMaxFailureTimes()) {
            return null;
        }
        return this.doLoginFailure(username);
    }

    @Override
    public boolean checkFailureTimes(String username) {
        //0不限制登陆失败次数
        if (0 == this.getMaxFailureTimes()) {
            return false;
        }
        return this.doCheckFailureTimes(username);
    }

    /**
     * 子类需要实现的登陆失败处理
     *
     * @param username
     * @return
     */
    abstract UserLoginLimitTracker doLoginFailure(String username);

    /**
     * 子类需要实现的检查失败次数
     *
     * @param username
     * @return
     */
    abstract boolean doCheckFailureTimes(String username);
}
