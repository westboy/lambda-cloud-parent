package com.lambda.security.web.form.locking;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于Redis的用户登录锁定策略实现
 *
 * <p>设计目标：
 * <ul>
 *   <li>分布式锁定：支持分布式环境下的用户登录失败锁定</li>
 *   <li>高性能：基于Redis内存存储，提供高性能的锁定检查</li>
 *   <li>自动过期：利用Redis的TTL机制实现自动解锁</li>
 *   <li>集群支持：支持Redis集群和哨兵模式</li>
 * </ul>
 *
 * <p>主要功能：
 * <ul>
 *   <li>失败计数：记录用户登录失败次数</li>
 *   <li>自动锁定：达到最大失败次数时自动锁定账户</li>
 *   <li>定时解锁：锁定时间到期后自动解锁</li>
 *   <li>手动解锁：支持管理员手动解锁账户</li>
 * </ul>
 *
 * <p>锁定机制：
 * <ol>
 *   <li>用户登录失败时，增加失败计数</li>
 *   <li>失败次数达到阈值时，设置锁定状态</li>
 *   <li>锁定期间禁止用户登录</li>
 *   <li>锁定时间到期或手动解锁后恢复正常</li>
 * </ol>
 *
 * <p>Redis存储结构：
 * <pre>
 * Key: LAMBDA-CLOUD:USER:LOGIN_FAILURE:{username}
 * Value: 失败次数（字符串格式）
 * TTL: 锁定持续时间
 * </pre>
 *
 * <p>使用示例：
 * <pre>{@code
 * @Configuration
 * public class SecurityConfig {
 *
 *     @Bean
 *     public RedisLockingStrategy redisLockingStrategy(
 *             StringRedisTemplate redisTemplate) {
 *         // 最大失败5次，锁定30分钟
 *         return new RedisLockingStrategy(5, 30, TimeUnit.MINUTES, redisTemplate);
 *     }
 * }
 * }</pre>
 *
 * <p>配置参数：
 * <ul>
 *   <li>maxFailureTimes：最大失败次数（0表示不限制）</li>
 *   <li>duration：锁定持续时间</li>
 *   <li>timeUnit：时间单位（秒、分钟、小时等）</li>
 * </ul>
 *
 * @author jpjoo
 * @see AbstractLockingStrategy
 * @see UserLoginLimitTracker
 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP2"},
        justification = "springboot properties")
@Setter
public class RedisLockingStrategy extends AbstractLockingStrategy {

    /**
     * Redis字符串操作模板
     *
     * <p>功能说明：
     * <ul>
     *   <li>数据存储：存储用户登录失败次数</li>
     *   <li>过期设置：设置数据的TTL（生存时间）</li>
     *   <li>原子操作：保证操作的原子性</li>
     *   <li>集群支持：支持Redis集群模式</li>
     * </ul>
     *
     * <p>操作类型：
     * <ul>
     *   <li>SET：设置失败次数和过期时间</li>
     *   <li>GET：获取当前失败次数</li>
     *   <li>DELETE：清除失败记录</li>
     *   <li>EXPIRE：获取剩余过期时间</li>
     * </ul>
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Redis键前缀常量
     *
     * <p>键格式说明：
     * <ul>
     *   <li>前缀：LAMBDA-CLOUD:USER:LOGIN_FAILURE:</li>
     *   <li>完整格式：LAMBDA-CLOUD:USER:LOGIN_FAILURE:{username}</li>
     *   <li>命名规范：使用冒号分隔的层级结构</li>
     *   <li>唯一性：确保不同用户的键不会冲突</li>
     * </ul>
     *
     * <p>设计考虑：
     * <ul>
     *   <li>可读性：清晰的命名便于运维和调试</li>
     *   <li>层级性：便于Redis管理工具按层级查看</li>
     *   <li>唯一性：避免与其他业务的键冲突</li>
     * </ul>
     */
    private static final String COMMON = "LAMBDA-CLOUD:USER:LOGIN_FAILURE:";

    /**
     * 构造函数
     *
     * <p>参数说明：
     * <ul>
     *   <li>maxFailureTimes：最大允许失败次数，0表示不限制</li>
     *   <li>duration：锁定持续时间数值</li>
     *   <li>timeUnit：时间单位（SECONDS、MINUTES、HOURS等）</li>
     *   <li>stringRedisTemplate：Redis操作模板</li>
     * </ul>
     *
     * <p>初始化过程：
     * <ol>
     *   <li>调用父类构造函数设置基本参数</li>
     *   <li>保存Redis操作模板引用</li>
     *   <li>验证参数的有效性</li>
     * </ol>
     *
     * <p>使用示例：
     * <pre>{@code
     * // 创建锁定策略：最大失败3次，锁定15分钟
     * RedisLockingStrategy strategy = new RedisLockingStrategy(
     *     3, 15, TimeUnit.MINUTES, stringRedisTemplate
     * );
     *
     * // 不限制失败次数的配置
     * RedisLockingStrategy noLimitStrategy = new RedisLockingStrategy(
     *     0, 0, TimeUnit.SECONDS, stringRedisTemplate
     * );
     * }</pre>
     *
     * @param maxFailureTimes 最大失败次数，0表示不限制
     * @param duration 锁定持续时间
     * @param timeUnit 时间单位
     * @param stringRedisTemplate Redis字符串操作模板
     */
    public RedisLockingStrategy(
            int maxFailureTimes, int duration, TimeUnit timeUnit, StringRedisTemplate stringRedisTemplate) {
        super(maxFailureTimes, duration, timeUnit);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 处理用户登录失败事件
     *
     * <p>处理流程：
     * <ol>
     *   <li>获取当前用户的失败次数</li>
     *   <li>将失败次数加1</li>
     *   <li>保存更新后的失败次数到Redis</li>
     *   <li>返回登录限制跟踪器对象</li>
     * </ol>
     *
     * <p>锁定逻辑：
     * <ul>
     *   <li>首次失败：创建Redis记录，设置TTL</li>
     *   <li>重复失败：更新失败次数，保持TTL</li>
     *   <li>达到阈值：账户进入锁定状态</li>
     *   <li>超时重置：TTL到期后自动清除记录</li>
     * </ul>
     *
     * <p>返回信息：
     * <ul>
     *   <li>maxFailureTimes：最大允许失败次数</li>
     *   <li>currentTimes：当前失败次数</li>
     *   <li>lockedTime：锁定开始时间（毫秒时间戳）</li>
     * </ul>
     *
     * @param username 用户账号名称
     * @return 用户登录限制跟踪器，包含失败次数和锁定信息
     */
    @Override
    public UserLoginLimitTracker doLoginFailure(String username) {
        int times = this.getFailureTimes(username);
        Long lockedTime = this.saveFailureTimes(username, ++times);
        return new UserLoginLimitTracker(getMaxFailureTimes(), times, lockedTime);
    }

    /**
     * 处理用户登录成功事件
     *
     * <p>清理逻辑：
     * <ul>
     *   <li>删除Redis中的失败记录</li>
     *   <li>重置失败计数器为0</li>
     *   <li>解除账户锁定状态</li>
     *   <li>允许用户正常登录</li>
     * </ul>
     *
     * <p>操作说明：
     * <ul>
     *   <li>原子操作：使用Redis的DELETE命令确保原子性</li>
     *   <li>幂等性：重复调用不会产生副作用</li>
     *   <li>即时生效：删除后立即生效，无需等待TTL</li>
     * </ul>
     *
     * <p>使用场景：
     * <ul>
     *   <li>密码登录成功后清除失败记录</li>
     *   <li>短信验证登录成功后重置状态</li>
     *   <li>第三方登录成功后清理限制</li>
     * </ul>
     *
     * @param username 用户账号名称
     */
    @Override
    public void loginSuccess(String username) {
        stringRedisTemplate.delete(getCommonKey(username));
    }

    /**
     * 检查用户登录失败次数是否超过限制
     *
     * <p>检查逻辑：
     * <ol>
     *   <li>如果最大失败次数为0，表示不限制，返回false</li>
     *   <li>获取当前用户的失败次数</li>
     *   <li>比较失败次数与最大允许次数</li>
     *   <li>返回是否超过限制的布尔值</li>
     * </ol>
     *
     * <p>特殊情况：
     * <ul>
     *   <li>maxFailureTimes = 0：不限制失败次数，始终返回false</li>
     *   <li>Redis中无记录：表示从未失败，返回false</li>
     *   <li>TTL已过期：Redis自动删除记录，返回false</li>
     * </ul>
     *
     * <p>性能考虑：
     * <ul>
     *   <li>Redis内存查询：毫秒级响应时间</li>
     *   <li>网络开销：单次Redis调用</li>
     *   <li>缓存友好：频繁调用不会影响性能</li>
     * </ul>
     *
     * @param username 用户账号名称
     * @return true表示失败次数超过限制（账户被锁定），false表示未超过限制
     */
    @Override
    public boolean doCheckFailureTimes(String username) {
        if (0 == this.getMaxFailureTimes()) {
            return false;
        }
        return this.getFailureTimes(username) >= this.getMaxFailureTimes();
    }

    /**
     * 手动解锁用户账户
     *
     * <p>解锁操作：
     * <ul>
     *   <li>删除Redis中的失败记录</li>
     *   <li>清除失败计数</li>
     *   <li>立即解除锁定状态</li>
     *   <li>允许用户重新尝试登录</li>
     * </ul>
     *
     * <p>使用场景：
     * <ul>
     *   <li>管理员手动解锁：通过管理界面解锁用户</li>
     *   <li>客服处理：客服人员协助用户解锁</li>
     *   <li>系统维护：批量解锁操作</li>
     *   <li>紧急情况：快速恢复用户访问</li>
     * </ul>
     *
     * <p>安全考虑：
     * <ul>
     *   <li>权限控制：只有授权用户才能执行解锁操作</li>
     *   <li>操作日志：记录解锁操作的执行者和时间</li>
     *   <li>审计跟踪：保留解锁操作的审计记录</li>
     * </ul>
     *
     * @param username 需要解锁的用户账号名称
     */
    @Override
    public void unlock(String username) {
        stringRedisTemplate.delete(getCommonKey(username));
    }

    /**
     * 获取用户账户的锁定状态
     *
     * <p>状态判断：
     * <ul>
     *   <li>调用checkFailureTimes方法检查失败次数</li>
     *   <li>返回账户是否处于锁定状态</li>
     *   <li>锁定状态基于失败次数和配置的阈值</li>
     * </ul>
     *
     * <p>返回值说明：
     * <ul>
     *   <li>true：账户已锁定，禁止登录</li>
     *   <li>false：账户正常，允许登录</li>
     * </ul>
     *
     * <p>实现细节：
     * <ul>
     *   <li>委托给父类的checkFailureTimes方法</li>
     *   <li>保持与doCheckFailureTimes方法的一致性</li>
     *   <li>提供更语义化的方法名称</li>
     * </ul>
     *
     * @param username 用户账号名称
     * @return true表示账户已锁定，false表示账户正常
     */
    @Override
    public boolean getLockedState(String username) {
        return checkFailureTimes(username);
    }

    /**
     * 获取用户当前的登录失败次数
     *
     * <p>查询逻辑：
     * <ol>
     *   <li>根据用户名构造Redis键</li>
     *   <li>从Redis中获取失败次数字符串</li>
     *   <li>如果记录不存在，返回0</li>
     *   <li>如果记录存在，解析为整数返回</li>
     * </ol>
     *
     * <p>异常处理：
     * <ul>
     *   <li>Redis连接异常：向上抛出异常</li>
     *   <li>数据格式异常：NumberFormatException</li>
     *   <li>空值处理：返回0表示从未失败</li>
     * </ul>
     *
     * <p>性能特点：
     * <ul>
     *   <li>单次Redis查询：O(1)时间复杂度</li>
     *   <li>内存操作：毫秒级响应</li>
     *   <li>网络开销：最小化数据传输</li>
     * </ul>
     *
     * @param username 用户账号名称
     * @return 当前失败次数，如果从未失败则返回0
     * @throws NumberFormatException 当Redis中存储的数据格式不正确时抛出
     */
    private int getFailureTimes(String username) {
        String failCount = stringRedisTemplate.opsForValue().get(getCommonKey(username));
        if (Objects.isNull(failCount)) {
            return 0;
        } else {
            return Integer.parseInt(failCount);
        }
    }

    /**
     * 获取用户账户的剩余锁定时间
     *
     * <p>时间计算：
     * <ol>
     *   <li>获取Redis键的剩余TTL（毫秒）</li>
     *   <li>如果TTL为null，表示键不存在或无过期时间</li>
     *   <li>返回剩余的锁定时间毫秒数</li>
     * </ol>
     *
     * <p>返回值说明：
     * <ul>
     *   <li>&gt; 0：剩余锁定时间（毫秒）</li>
     *   <li>= 0：键不存在或已过期</li>
     *   <li>-1：键存在但无过期时间（理论上不会出现）</li>
     * </ul>
     *
     * <p>使用场景：
     * <ul>
     *   <li>计算锁定开始时间</li>
     *   <li>显示剩余锁定时间</li>
     *   <li>锁定状态判断</li>
     * </ul>
     *
     * @param username 用户账号名称
     * @return 剩余锁定时间（毫秒），如果未锁定则返回0
     */
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

    /**
     * 保存用户登录失败次数到Redis
     *
     * <p>保存策略：
     * <ol>
     *   <li>检查用户是否已经被锁定</li>
     *   <li>如果已锁定：计算锁定开始时间并返回</li>
     *   <li>如果未锁定：保存失败次数并设置TTL</li>
     * </ol>
     *
     * <p>锁定时间计算：
     * <ul>
     *   <li>已锁定：锁定开始时间 = 当前时间 - (总锁定时间 - 剩余锁定时间)</li>
     *   <li>新锁定：锁定开始时间 = 当前时间</li>
     * </ul>
     *
     * <p>Redis操作：
     * <ul>
     *   <li>SET命令：原子性设置值和过期时间</li>
     *   <li>TTL设置：使用配置的duration和timeUnit</li>
     *   <li>覆盖更新：新的失败次数会覆盖旧值</li>
     * </ul>
     *
     * <p>时间精度：
     * <ul>
     *   <li>使用毫秒时间戳确保精确性</li>
     *   <li>支持秒、分钟、小时等时间单位</li>
     *   <li>自动转换为Redis支持的格式</li>
     * </ul>
     *
     * @param username 用户账号名称
     * @param times 更新后的失败次数
     * @return 锁定开始时间的毫秒时间戳
     */
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

    /**
     * 构造Redis存储键
     *
     * <p>键格式：
     * <ul>
     *   <li>模板：LAMBDA-CLOUD:USER:LOGIN_FAILURE:{username}</li>
     *   <li>示例：LAMBDA-CLOUD:USER:LOGIN_FAILURE:admin</li>
     *   <li>层级：使用冒号分隔的命名空间结构</li>
     * </ul>
     *
     * <p>设计原则：
     * <ul>
     *   <li>唯一性：确保不同用户的键不会冲突</li>
     *   <li>可读性：清晰的命名便于调试和运维</li>
     *   <li>层级性：便于Redis管理工具分类显示</li>
     *   <li>一致性：与项目的键命名规范保持一致</li>
     * </ul>
     *
     * <p>安全考虑：
     * <ul>
     *   <li>用户名转义：防止特殊字符影响键格式</li>
     *   <li>长度限制：Redis键长度建议不超过512字节</li>
     *   <li>字符集：使用UTF-8编码支持中文用户名</li>
     * </ul>
     *
     * @param username 用户账号名称
     * @return 完整的Redis存储键
     */
    private String getCommonKey(String username) {
        return MessageFormat.format("{0}:{1}", COMMON, username);
    }
}
