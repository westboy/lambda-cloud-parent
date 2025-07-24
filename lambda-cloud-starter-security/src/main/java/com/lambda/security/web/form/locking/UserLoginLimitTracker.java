package com.lambda.security.web.form.locking;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户登录限制跟踪器
 *
 * <p>用于跟踪和管理用户登录失败次数的数据模型。该类封装了用户登录失败的相关信息，
 * 包括最大失败次数、当前失败次数、锁定时间、过期时间等，并提供了相应的业务逻辑方法。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>状态跟踪</strong> - 跟踪用户的登录失败状态和锁定状态</li>
 *   <li><strong>业务封装</strong> - 封装登录限制相关的业务逻辑</li>
 *   <li><strong>数据模型</strong> - 作为登录限制数据的载体</li>
 *   <li><strong>状态计算</strong> - 提供锁定状态和剩余次数的计算</li>
 * </ul>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><strong>失败计数</strong> - 记录和管理登录失败次数</li>
 *   <li><strong>锁定判断</strong> - 判断用户是否应该被锁定</li>
 *   <li><strong>时间管理</strong> - 管理锁定时间和过期时间</li>
 *   <li><strong>剩余次数</strong> - 计算用户还可以尝试的次数</li>
 * </ul>
 *
 * <h3>状态说明</h3>
 * <ul>
 *   <li><strong>正常状态</strong> - failCount < maxFailCount，用户可以正常登录</li>
 *   <li><strong>锁定状态</strong> - failCount >= maxFailCount，用户被锁定</li>
 *   <li><strong>过期状态</strong> - 锁定时间已过期，可以重新尝试</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建跟踪器
 * UserLoginLimitTracker tracker = new UserLoginLimitTracker(5);
 *
 * // 记录失败
 * boolean locked = tracker.failOnce();
 * if (locked) {
 *     System.out.println("用户已被锁定");
 * }
 *
 * // 检查剩余次数
 * Integer leftTimes = tracker.getLeftTimes();
 * System.out.println("剩余尝试次数: " + leftTimes);
 * }</pre>
 *
 * @author jpjoo
 * @see FormLockingStrategy
 * @see AbstractLockingStrategy
 */
public class UserLoginLimitTracker {
    /**
     * 最大失败次数
     *
     * <p>用户登录失败的最大允许次数，超过此次数将被锁定。
     * 该值在创建跟踪器时设定，不可修改。</p>
     */
    private final Integer maxFailCount;

    /**
     * 当前失败次数
     *
     * <p>用户当前的登录失败次数，每次登录失败时递增。
     * 当达到maxFailCount时，用户将被锁定。</p>
     */
    private int failCount;

    /**
     * 锁定时间戳
     *
     * <p>用户被锁定的时间戳（毫秒）。当用户首次达到最大失败次数时，
     * 系统会自动设置此时间戳。用于计算锁定持续时间。</p>
     */
    @Getter
    private Long lockedTime;

    /**
     * 过期时间
     *
     * <p>跟踪器数据的过期时间。超过此时间后，跟踪器数据将被清理，
     * 用户可以重新开始登录尝试。通常用于缓存管理。</p>
     */
    @Setter
    @Getter
    private LocalDateTime expireTime;

    /**
     * 构造一个新的用户登录限制跟踪器
     *
     * <p>创建一个初始状态的跟踪器，失败次数为0，未锁定状态。
     * 通常用于新用户或重置用户登录状态时。</p>
     *
     * @param maxFailCount 最大失败次数，不能为null且应大于0
     */
    public UserLoginLimitTracker(Integer maxFailCount) {
        this.maxFailCount = maxFailCount;
        this.failCount = 0;
    }

    /**
     * 构造一个带有历史状态的用户登录限制跟踪器
     *
     * <p>用于从缓存或数据库中恢复用户的登录限制状态。
     * 通常在系统重启或从持久化存储中加载数据时使用。</p>
     *
     * @param maxFailCount 最大失败次数，不能为null且应大于0
     * @param failCount 当前失败次数，应在0到maxFailCount之间
     * @param lockedTime 锁定时间戳，如果用户未被锁定则为null
     */
    public UserLoginLimitTracker(Integer maxFailCount, int failCount, Long lockedTime) {
        this.maxFailCount = maxFailCount;
        this.failCount = failCount;
        this.lockedTime = lockedTime;
    }

    /**
     * 检查用户是否被锁定
     *
     * <p>判断当前用户是否因为登录失败次数过多而被锁定。
     * 如果失败次数达到或超过最大失败次数，则认为用户被锁定。</p>
     *
     * <p><strong>副作用：</strong>如果用户应该被锁定但锁定时间戳为空，
     * 此方法会自动设置锁定时间戳为当前时间。</p>
     *
     * @return true表示用户被锁定，false表示用户未被锁定
     */
    public boolean isLocked() {
        if (failCount >= maxFailCount) {
            if (this.lockedTime == null) {
                this.lockedTime = System.currentTimeMillis();
            }
            return true;
        }
        return false;
    }

    /**
     * 记录一次登录失败
     *
     * <p>将失败次数加1，并检查用户是否应该被锁定。
     * 这是记录用户登录失败的主要方法。</p>
     *
     * <p><strong>处理流程：</strong></p>
     * <ol>
     *   <li>失败次数加1</li>
     *   <li>检查是否达到锁定条件</li>
     *   <li>如果达到锁定条件且未设置锁定时间，则设置锁定时间</li>
     *   <li>返回锁定状态</li>
     * </ol>
     *
     * @return true表示用户已被锁定，false表示用户仍可继续尝试
     */
    public boolean failOnce() {
        failCount++;
        return isLocked();
    }

    /**
     * 获取剩余尝试次数
     *
     * <p>计算用户还可以尝试登录的次数。如果返回值为0或负数，
     * 表示用户已经没有剩余尝试次数，应该被锁定。</p>
     *
     * <p><strong>计算公式：</strong>剩余次数 = 最大失败次数 - 当前失败次数</p>
     *
     * @return 剩余的登录尝试次数，0或负数表示无剩余次数
     */
    public Integer getLeftTimes() {
        return maxFailCount - failCount;
    }
}
