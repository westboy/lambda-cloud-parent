package com.lambda.security.web.verify.service.captcha.store;

import java.util.concurrent.TimeUnit;

/**
 * 图形验证码存储接口
 *
 * <p>设计目标：
 * <ul>
 *   <li>存储抽象：为验证码存储提供统一的抽象接口</li>
 *   <li>多种实现：支持内存、Redis、数据库等多种存储方式</li>
 *   <li>时效管理：支持验证码的自动过期和清理</li>
 *   <li>高性能：提供高效的存储和验证操作</li>
 * </ul>
 *
 * <p>主要功能：
 * <ul>
 *   <li>验证码存储：将生成的验证码与令牌关联存储</li>
 *   <li>过期控制：设置验证码的有效期限</li>
 *   <li>验证校对：验证用户输入与存储的验证码是否匹配</li>
 *   <li>自动清理：过期验证码的自动清理机制</li>
 * </ul>
 *
 * <p>存储模型：
 * <ul>
 *   <li>键值对：token作为键，verifyCode作为值</li>
 *   <li>时效性：每个验证码都有独立的过期时间</li>
 *   <li>一次性：验证成功后立即删除，防止重复使用</li>
 *   <li>线程安全：支持并发访问和操作</li>
 * </ul>
 *
 * <p>实现示例：
 * <pre>{@code
 * @Component
 * public class RedisCaptchaStore implements CaptchaStore {
 *
 *     @Autowired
 *     private StringRedisTemplate redisTemplate;
 *
 *     @Override
 *     public void store(String token, String verifyCode, TimeUnit timeUnit, Integer time) {
 *         redisTemplate.opsForValue().set("captcha:" + token, verifyCode, time, timeUnit);
 *     }
 *
 *     @Override
 *     public boolean validate(String token, String inputCode) {
 *         String stored = redisTemplate.opsForValue().get("captcha:" + token);
 *         if (stored != null && stored.equalsIgnoreCase(inputCode)) {
 *             redisTemplate.delete("captcha:" + token);
 *             return true;
 *         }
 *         return false;
 *     }
 * }
 * }</pre>
 *
 * <p>使用场景：
 * <ul>
 *   <li>登录验证：用户登录时的图形验证码验证</li>
 *   <li>注册保护：防止恶意注册和机器人攻击</li>
 *   <li>敏感操作：重要操作前的二次验证</li>
 *   <li>防刷机制：防止接口被恶意调用</li>
 * </ul>
 *
 * <p>存储策略：
 * <ul>
 *   <li>内存存储：适用于单机部署，性能最高</li>
 *   <li>Redis存储：适用于分布式部署，支持集群</li>
 *   <li>数据库存储：适用于持久化要求高的场景</li>
 *   <li>混合存储：结合多种存储方式的优势</li>
 * </ul>
 *
 * @author jpjoo
 * @see java.util.concurrent.TimeUnit
 */
public interface CaptchaStore {

    /**
     * 存储验证码到指定的存储介质
     *
     * <p>存储逻辑：
     * <ul>
     *   <li>键值关联：将token作为唯一键，verifyCode作为值进行存储</li>
     *   <li>时效设置：根据timeUnit和time参数设置过期时间</li>
     *   <li>覆盖策略：如果token已存在，新的验证码会覆盖旧的</li>
     *   <li>异步清理：过期后自动清理，无需手动删除</li>
     * </ul>
     *
     * <p>存储特性：
     * <ul>
     *   <li>原子操作：存储操作保证原子性</li>
     *   <li>线程安全：支持并发存储操作</li>
     *   <li>持久化：根据实现方式决定是否持久化</li>
     *   <li>高可用：支持集群和故障转移</li>
     * </ul>
     *
     * <p>时间单位示例：
     * <ul>
     *   <li>秒级：TimeUnit.SECONDS, 300 (5分钟)</li>
     *   <li>分钟级：TimeUnit.MINUTES, 5 (5分钟)</li>
     *   <li>小时级：TimeUnit.HOURS, 1 (1小时)</li>
     *   <li>天级：TimeUnit.DAYS, 1 (1天)</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>{@code
     * // 存储验证码，5分钟后过期
     * captchaStore.store("user123_captcha", "ABCD", TimeUnit.MINUTES, 5);
     *
     * // 存储验证码，300秒后过期
     * captchaStore.store("session456", "1234", TimeUnit.SECONDS, 300);
     * }</pre>
     *
     * <p>注意事项：
     * <ul>
     *   <li>token唯一性：确保token在系统中的唯一性</li>
     *   <li>验证码格式：支持数字、字母、混合等多种格式</li>
     *   <li>过期时间：建议设置合理的过期时间，既保证安全又不影响用户体验</li>
     *   <li>存储容量：注意存储空间的使用，避免内存泄漏</li>
     * </ul>
     *
     * @param token 验证码的唯一标识符，通常与用户会话或请求相关联
     * @param verifyCode 生成的验证码内容，可以是数字、字母或混合字符
     * @param timeUnit 时间单位，如秒、分钟、小时等
     * @param time 过期时间数值，与timeUnit配合使用
     */
    void store(String token, String verifyCode, TimeUnit timeUnit, Integer time);

    /**
     * 验证用户输入的验证码是否正确
     *
     * <p>验证流程：
     * <ol>
     *   <li>根据token查找存储的验证码</li>
     *   <li>比较用户输入与存储的验证码</li>
     *   <li>验证成功后删除验证码（一次性使用）</li>
     *   <li>返回验证结果</li>
     * </ol>
     *
     * <p>验证规则：
     * <ul>
     *   <li>精确匹配：默认区分大小写的精确匹配</li>
     *   <li>忽略大小写：部分实现可能支持忽略大小写</li>
     *   <li>空值处理：空输入或null值视为验证失败</li>
     *   <li>过期检查：自动检查验证码是否已过期</li>
     * </ul>
     *
     * <p>安全特性：
     * <ul>
     *   <li>一次性使用：验证成功后立即删除，防止重复使用</li>
     *   <li>防暴力破解：可结合失败次数限制</li>
     *   <li>时效保护：过期验证码自动失效</li>
     *   <li>令牌隔离：不同token的验证码相互独立</li>
     * </ul>
     *
     * <p>验证结果：
     * <ul>
     *   <li>true：验证码正确且在有效期内</li>
     *   <li>false：验证码错误、已过期、不存在或已使用</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>{@code
     * // 验证用户输入的验证码
     * boolean isValid = captchaStore.validate("user123_captcha", "ABCD");
     * if (isValid) {
     *     // 验证成功，继续业务逻辑
     *     processLogin(user);
     * } else {
     *     // 验证失败，返回错误信息
     *     return "验证码错误或已过期";
     * }
     * }</pre>
     *
     * <p>错误场景：
     * <ul>
     *   <li>验证码不存在：token对应的验证码未找到</li>
     *   <li>验证码错误：用户输入与存储的验证码不匹配</li>
     *   <li>验证码过期：验证码已超过设定的有效期</li>
     *   <li>验证码已用：验证码已被使用过（一次性）</li>
     * </ul>
     *
     * <p>性能考虑：
     * <ul>
     *   <li>快速查找：使用高效的数据结构进行查找</li>
     *   <li>批量清理：定期清理过期的验证码</li>
     *   <li>内存优化：及时释放已使用的验证码内存</li>
     *   <li>并发安全：支持多线程并发验证</li>
     * </ul>
     *
     * @param token 验证码的唯一标识符，与存储时使用的token相同
     * @param inputCode 用户输入的验证码内容
     * @return true表示验证成功，false表示验证失败
     */
    boolean validate(String token, String inputCode);
}
