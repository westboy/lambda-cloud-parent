package com.jingfang.security.web.authentication.locking;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 用户登陆限制记录器
 *
 * @author jpjoo
 */
public class UserLoginLimitTracker {
    private final Integer maxFailCount;
    private int failCount;
    @Getter
    private Long lockedTime;

    /**
     * 失效时间
     */
    @Setter
    @Getter
    private LocalDateTime expireTime;

    public UserLoginLimitTracker(Integer maxFailCount) {
        this.maxFailCount = maxFailCount;
        this.failCount = 0;
    }

    public UserLoginLimitTracker(Integer maxFailCount, int failCount, Long lockedTime) {
        this.maxFailCount = maxFailCount;
        this.failCount = failCount;
        this.lockedTime = lockedTime;
    }

    public boolean isLocked() {
        if (failCount >= maxFailCount) {
            if (this.lockedTime == null) {
                this.lockedTime = System.currentTimeMillis();
            }
            return true;
        }
        return false;
    }

    public boolean failOnce() {
        failCount++;
        return isLocked();
    }

    public Integer getLeftTimes() {
        return maxFailCount - failCount;
    }

}
