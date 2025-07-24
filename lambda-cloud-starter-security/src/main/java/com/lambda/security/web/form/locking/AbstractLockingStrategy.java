package com.lambda.security.web.form.locking;

import com.lambda.security.web.form.FormLockingStrategy;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;

/**
 * 抽象锁定策略基类
 *
 * <p>这是FormLockingStrategy接口的抽象实现，提供了锁定策略的通用框架和基础功能。
 * 子类只需要实现具体的存储逻辑，而通用的业务逻辑由基类处理。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>模板方法模式</strong> - 定义锁定策略的算法骨架，具体实现由子类完成</li>
 *   <li><strong>配置管理</strong> - 统一管理最大失败次数、锁定时间等配置参数</li>
 *   <li><strong>业务逻辑封装</strong> - 封装通用的业务逻辑，如零值检查、参数验证等</li>
 *   <li><strong>扩展性</strong> - 为不同的存储实现（内存、Redis、数据库等）提供统一基础</li>
 * </ul>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><strong>参数管理</strong> - 管理最大失败次数、锁定时间、时间单位等配置</li>
 *   <li><strong>零值处理</strong> - 当最大失败次数为0时，表示不限制登录失败</li>
 *   <li><strong>模板方法</strong> - 提供loginFailure和checkFailureTimes的模板实现</li>
 *   <li><strong>抽象接口</strong> - 定义子类必须实现的核心方法</li>
 * </ul>
 *
 * <h3>配置说明</h3>
 * <ul>
 *   <li><strong>maxFailureTimes</strong> - 最大失败次数，0表示不限制</li>
 *   <li><strong>duration</strong> - 锁定持续时间</li>
 *   <li><strong>timeUnit</strong> - 时间单位（秒、分钟、小时等）</li>
 * </ul>
 *
 * <h3>子类实现示例</h3>
 * <pre>{@code
 * public class RedisLockingStrategy extends AbstractLockingStrategy {
 *     private RedisTemplate<String, Object> redisTemplate;
 *
 *     public RedisLockingStrategy(int maxFailureTimes, int duration, TimeUnit timeUnit) {
 *         super(maxFailureTimes, duration, timeUnit);
 *     }
 *
 *     @Override
 *     protected UserLoginLimitTracker doLoginFailure(String username) {
 *         String key = "login_failure:" + username;
 *         Long count = redisTemplate.opsForValue().increment(key);
 *         redisTemplate.expire(key, getDuration(), getTimeUnit());
 *         return new UserLoginLimitTracker(username, count.intValue());
 *     }
 *
 *     @Override
 *     protected boolean doCheckFailureTimes(String username) {
 *         String key = "login_failure:" + username;
 *         Integer count = (Integer) redisTemplate.opsForValue().get(key);
 *         return count != null && count >= getMaxFailureTimes();
 *     }
 * }
 * }</pre>
 *
 * @author jpjoo
 * @see FormLockingStrategy
 * @see UserLoginLimitTracker
 */
@Setter
public abstract class AbstractLockingStrategy implements FormLockingStrategy {

    /** 最大失败次数，0表示不限制登录失败次数 */
    @Getter
    private int maxFailureTimes;

    /** 锁定持续时间 */
    private int duration;

    /** 时间单位 */
    private TimeUnit timeUnit;

    /**
     * 构造函数
     *
     * @param maxFailureTimes 最大失败次数，0表示不限制
     * @param duration        锁定持续时间
     * @param timeUnit        时间单位
     */
    protected AbstractLockingStrategy(int maxFailureTimes, int duration, TimeUnit timeUnit) {
        this.maxFailureTimes = maxFailureTimes;
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    /**
     * 获取锁定持续时间
     *
     * @return 锁定持续时间
     */
    @Override
    public int getDuration() {
        return duration;
    }

    /**
     * 获取时间单位
     *
     * @return 时间单位
     */
    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * 处理登录失败事件（模板方法）
     *
     * <p>这是模板方法的实现，首先检查是否启用了失败次数限制，
     * 如果启用则委托给子类的具体实现。</p>
     *
     * @param username 登录失败的用户名
     * @return 用户登录限制跟踪器，如果不限制则返回null
     */
    @Override
    public UserLoginLimitTracker loginFailure(String username) {
        // 0不限制登陆失败次数
        if (0 == this.getMaxFailureTimes()) {
            return null;
        }
        return this.doLoginFailure(username);
    }

    /**
     * 检查失败次数是否超限（模板方法）
     *
     * <p>这是模板方法的实现，首先检查是否启用了失败次数限制，
     * 如果启用则委托给子类的具体实现。</p>
     *
     * @param username 要检查的用户名
     * @return true表示超限应该锁定，false表示未超限
     */
    @Override
    public boolean checkFailureTimes(String username) {
        // 0不限制登陆失败次数
        if (0 == this.getMaxFailureTimes()) {
            return false;
        }
        return this.doCheckFailureTimes(username);
    }

    /**
     * 子类实现的具体登录失败处理逻辑
     *
     * <p>子类需要实现此方法来处理具体的登录失败逻辑，包括：</p>
     * <ul>
     *   <li>增加失败计数</li>
     *   <li>设置过期时间</li>
     *   <li>返回跟踪器对象</li>
     * </ul>
     *
     * @param username 登录失败的用户名
     * @return 用户登录限制跟踪器
     */
    abstract UserLoginLimitTracker doLoginFailure(String username);

    /**
     * 子类实现的具体失败次数检查逻辑
     *
     * <p>子类需要实现此方法来检查用户的失败次数是否超过限制，包括：</p>
     * <ul>
     *   <li>获取当前失败次数</li>
     *   <li>与最大失败次数比较</li>
     *   <li>检查锁定时间是否有效</li>
     * </ul>
     *
     * @param username 要检查的用户名
     * @return true表示失败次数超限，false表示未超限
     */
    abstract boolean doCheckFailureTimes(String username);
}
