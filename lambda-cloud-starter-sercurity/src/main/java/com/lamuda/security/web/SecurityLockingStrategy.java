package com.lambda.security.web;

import com.lambda.security.web.authentication.locking.UserLoginLimitTracker;

import java.util.concurrent.TimeUnit;

/**
 * SecurityLockingStrategy
 *
 * @author jpjoo
 */
public interface SecurityLockingStrategy {
    /***
     * 登陆失败缓存处理
     *
     * @param username
     *
     * @return
     */
    UserLoginLimitTracker loginFailure(String username);

    /***
     * 登陆成功缓存处理
     *
     * @param username
     *
     * @return
     */
    void loginSuccess(String username);

    /**
     * 验证用户登陆次数是否超额
     *
     * @param username
     *
     * @return
     */
    boolean checkFailureTimes(String username);

    /**
     * 解锁用户
     *
     * @param username
     *
     * @return
     */
    void unlock(String username);

    /**
     * 获取用户锁定状态
     *
     * @param username
     *
     * @return
     */
    boolean getLockedState(String username);

    /**
     * 获取持续时间
     *
     * @return
     */
    int getDuration();

    /**
     * 获取时间单位
     *
     * @return
     */
    TimeUnit getTimeUnit();

}
