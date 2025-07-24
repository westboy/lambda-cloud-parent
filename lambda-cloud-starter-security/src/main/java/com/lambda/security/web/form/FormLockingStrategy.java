package com.lambda.security.web.form;

import com.lambda.security.web.form.locking.UserLoginLimitTracker;
import java.util.concurrent.TimeUnit;

/**
 * 表单锁定策略接口
 *
 * <p>定义了用于防止暴力破解攻击的账户锁定策略标准。该接口提供了登录失败处理、
 * 账户锁定检查、锁定状态管理等功能，是安全防护体系的重要组成部分。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>暴力破解防护</strong> - 通过限制登录失败次数防止密码暴力破解</li>
 *   <li><strong>灵活策略</strong> - 支持不同的锁定策略实现（内存、Redis等）</li>
 *   <li><strong>可配置性</strong> - 支持自定义锁定时间和失败次数阈值</li>
 *   <li><strong>状态管理</strong> - 提供完整的锁定状态查询和管理功能</li>
 * </ul>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><strong>失败计数</strong> - 记录和累计用户登录失败次数</li>
 *   <li><strong>锁定检查</strong> - 检查用户是否因失败次数过多而被锁定</li>
 *   <li><strong>状态重置</strong> - 登录成功时重置失败计数</li>
 *   <li><strong>手动解锁</strong> - 支持管理员手动解锁被锁定的账户</li>
 *   <li><strong>配置查询</strong> - 提供锁定时间和时间单位的查询</li>
 * </ul>
 *
 * <h3>典型锁定策略</h3>
 * <ul>
 *   <li><strong>固定时间锁定</strong> - 失败次数达到阈值后锁定固定时间</li>
 *   <li><strong>递增时间锁定</strong> - 每次失败后锁定时间递增</li>
 *   <li><strong>永久锁定</strong> - 达到阈值后需要管理员手动解锁</li>
 *   <li><strong>滑动窗口</strong> - 在时间窗口内统计失败次数</li>
 * </ul>
 *
 * <h3>实现示例</h3>
 * <pre>{@code
 * @Component
 * public class RedisFormLockingStrategy implements FormLockingStrategy {
 *     private static final int MAX_ATTEMPTS = 5;
 *     private static final int LOCK_DURATION = 30;
 *
 *     @Override
 *     public boolean checkFailureTimes(String username) {
 *         int attempts = getFailureCount(username);
 *         return attempts >= MAX_ATTEMPTS;
 *     }
 *
 *     @Override
 *     public UserLoginLimitTracker loginFailure(String username) {
 *         return incrementFailureCount(username);
 *     }
 *
 *     @Override
 *     public void loginSuccess(String username) {
 *         clearFailureCount(username);
 *     }
 * }
 * }</pre>
 *
 * <h3>安全考虑</h3>
 * <ul>
 *   <li><strong>分布式一致性</strong> - 在集群环境中确保锁定状态的一致性</li>
 *   <li><strong>性能优化</strong> - 避免频繁的存储访问影响性能</li>
 *   <li><strong>误锁防护</strong> - 防止正常用户被恶意锁定</li>
 *   <li><strong>监控告警</strong> - 对异常的锁定行为进行监控和告警</li>
 * </ul>
 *
 * @author jpjoo
 * @see UserLoginLimitTracker
 * @see FormAuthenticationProcessingFilter
 */
public interface FormLockingStrategy {
    /**
     * 处理登录失败事件
     *
     * <p>当用户登录失败时调用此方法，用于记录失败次数并更新锁定状态。
     * 该方法会增加用户的失败计数，并根据策略决定是否需要锁定账户。</p>
     *
     * <h4>处理逻辑</h4>
     * <ul>
     *   <li><strong>计数增加</strong> - 将用户的失败次数加1</li>
     *   <li><strong>状态更新</strong> - 更新用户的锁定状态和时间戳</li>
     *   <li><strong>阈值检查</strong> - 检查是否达到锁定阈值</li>
     *   <li><strong>锁定执行</strong> - 如果达到阈值则执行锁定操作</li>
     * </ul>
     *
     * @param username 登录失败的用户名
     * @return 更新后的用户登录限制跟踪器
     */
    UserLoginLimitTracker loginFailure(String username);

    /**
     * 处理登录成功事件
     *
     * <p>当用户登录成功时调用此方法，用于清除失败计数和解除锁定状态。
     * 这是一个重置操作，会将用户的所有失败记录清零。</p>
     *
     * <h4>处理逻辑</h4>
     * <ul>
     *   <li><strong>计数清零</strong> - 将失败次数重置为0</li>
     *   <li><strong>解除锁定</strong> - 如果用户被锁定，则解除锁定状态</li>
     *   <li><strong>清理缓存</strong> - 清理相关的临时数据</li>
     * </ul>
     *
     * @param username 登录成功的用户名
     */
    void loginSuccess(String username);

    /**
     * 检查用户登录失败次数是否超过限制
     *
     * <p>检查指定用户的登录失败次数是否已经达到或超过配置的阈值，
     * 如果超过则表示用户应该被锁定。</p>
     *
     * <h4>检查逻辑</h4>
     * <ul>
     *   <li><strong>次数获取</strong> - 获取用户当前的失败次数</li>
     *   <li><strong>阈值比较</strong> - 与配置的最大失败次数进行比较</li>
     *   <li><strong>时间检查</strong> - 检查锁定时间是否已过期</li>
     *   <li><strong>状态判断</strong> - 综合判断是否应该被锁定</li>
     * </ul>
     *
     * @param username 要检查的用户名
     * @return true表示失败次数超限应该被锁定，false表示未超限
     */
    boolean checkFailureTimes(String username);

    /**
     * 手动解锁用户账户
     *
     * <p>管理员可以使用此方法手动解锁被锁定的用户账户，
     * 这通常用于紧急情况或误锁的处理。</p>
     *
     * <h4>解锁逻辑</h4>
     * <ul>
     *   <li><strong>状态重置</strong> - 重置用户的锁定状态</li>
     *   <li><strong>计数清零</strong> - 清除失败次数记录</li>
     *   <li><strong>时间清理</strong> - 清除锁定时间戳</li>
     *   <li><strong>日志记录</strong> - 记录解锁操作的审计日志</li>
     * </ul>
     *
     * @param username 要解锁的用户名
     */
    void unlock(String username);

    /**
     * 获取用户当前的锁定状态
     *
     * <p>查询指定用户当前是否处于锁定状态。这个方法会检查用户的锁定时间
     * 是否已过期，如果已过期则自动解锁。</p>
     *
     * <h4>状态判断逻辑</h4>
     * <ul>
     *   <li><strong>锁定记录检查</strong> - 检查是否存在锁定记录</li>
     *   <li><strong>时间有效性</strong> - 检查锁定时间是否仍然有效</li>
     *   <li><strong>自动解锁</strong> - 如果锁定时间已过期则自动解锁</li>
     * </ul>
     *
     * @param username 要查询的用户名
     * @return true表示用户当前被锁定，false表示未被锁定
     */
    boolean getLockedState(String username);

    /**
     * 获取锁定持续时间
     *
     * <p>返回账户锁定的持续时间数值。这个时间与getTimeUnit()方法返回的
     * 时间单位配合使用，构成完整的锁定时长配置。</p>
     *
     * @return 锁定持续时间的数值部分
     */
    int getDuration();

    /**
     * 获取锁定时间的时间单位
     *
     * <p>返回锁定持续时间的时间单位，如秒、分钟、小时等。
     * 与getDuration()方法配合使用确定完整的锁定时长。</p>
     *
     * @return 时间单位枚举值
     */
    TimeUnit getTimeUnit();
}
