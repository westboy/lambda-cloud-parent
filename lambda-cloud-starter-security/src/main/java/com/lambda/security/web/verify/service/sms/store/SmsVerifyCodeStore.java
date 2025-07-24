package com.lambda.security.web.verify.service.sms.store;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.lambda.security.web.verify.service.sms.model.SmsVerifyCode;
import java.time.LocalDateTime;

/**
 * 短信验证码存储接口
 * <p>
 * 定义短信验证码的存储、验证和管理规范。该接口提供了验证码生命周期管理的标准方法，
 * 包括生成、存储、验证、重发控制等功能。支持泛型设计，可以适配不同类型的验证码。
 * </p>
 *
 * <h3>设计目标：</h3>
 * <ul>
 *   <li><strong>标准化</strong> - 提供统一的验证码存储接口规范</li>
 *   <li><strong>灵活性</strong> - 支持不同的存储实现（内存、Redis、数据库等）</li>
 *   <li><strong>类型安全</strong> - 使用泛型确保验证码类型的一致性</li>
 *   <li><strong>扩展性</strong> - 支持自定义验证码格式和存储策略</li>
 * </ul>
 *
 * <h3>核心功能：</h3>
 * <ul>
 *   <li><strong>验证码生成</strong> - 生成指定格式的验证码</li>
 *   <li><strong>验证码存储</strong> - 将验证码存储到指定的存储介质</li>
 *   <li><strong>验证码验证</strong> - 验证用户输入的验证码是否正确</li>
 *   <li><strong>重发控制</strong> - 控制验证码的重发频率</li>
 *   <li><strong>过期管理</strong> - 管理验证码的有效期</li>
 * </ul>
 *
 * <h3>生命周期管理：</h3>
 * <ol>
 *   <li><strong>生成阶段</strong> - 调用generate()方法生成验证码</li>
 *   <li><strong>存储阶段</strong> - 验证码自动存储到存储介质</li>
 *   <li><strong>验证阶段</strong> - 调用verify()方法验证用户输入</li>
 *   <li><strong>清理阶段</strong> - 验证成功或过期后自动清理</li>
 * </ol>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li><strong>时效性</strong> - 验证码具有明确的有效期限制</li>
 *   <li><strong>频率控制</strong> - 防止验证码被频繁发送</li>
 *   <li><strong>一次性使用</strong> - 验证成功后立即失效</li>
 *   <li><strong>防暴力破解</strong> - 通过时间限制降低暴力破解风险</li>
 * </ul>
 *
 * <h3>实现建议：</h3>
 * <ul>
 *   <li><strong>线程安全</strong> - 实现类应确保多线程环境下的安全性</li>
 *   <li><strong>异常处理</strong> - 妥善处理存储异常和网络异常</li>
 *   <li><strong>性能优化</strong> - 考虑高并发场景下的性能表现</li>
 *   <li><strong>监控日志</strong> - 记录关键操作便于问题排查</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 生成验证码
 * String code = store.generate("13800138000");
 *
 * // 验证验证码
 * boolean isValid = store.verify("13800138000", "1234");
 *
 * // 检查是否可以重发
 * boolean canResend = store.verifyReSend("13800138000");
 * }</pre>
 *
 * @param <T> 验证码类型，通常为String
 * @author Jin
 * @see SmsVerifyCode
 * @see RedisSmsVerifyCodeStore
 * @since 1.0.0
 */
public interface SmsVerifyCodeStore<T> {

    /**
     * 生成验证码
     * <p>
     * 为指定的键生成一个新的验证码，并将其存储到存储介质中。
     * 每次调用都会生成一个新的验证码，覆盖之前的验证码（如果存在）。
     * </p>
     *
     * <h3>生成规则：</h3>
     * <ul>
     *   <li><strong>唯一性</strong> - 每次生成的验证码都是唯一的</li>
     *   <li><strong>随机性</strong> - 使用安全的随机数生成算法</li>
     *   <li><strong>格式化</strong> - 根据实现类的规则格式化验证码</li>
     * </ul>
     *
     * <h3>存储行为：</h3>
     * <ul>
     *   <li><strong>自动存储</strong> - 生成后自动存储到存储介质</li>
     *   <li><strong>设置过期</strong> - 根据getPeriod()设置过期时间</li>
     *   <li><strong>覆盖旧值</strong> - 新验证码会覆盖同键的旧验证码</li>
     * </ul>
     *
     * @param key 验证码标识符，通常为手机号或用户标识
     * @return 生成的验证码
     */
    T generate(String key);

    /**
     * 验证验证码
     * <p>
     * 验证用户输入的验证码是否与存储的验证码匹配。
     * 验证成功后，验证码通常会被标记为已使用或直接删除，确保一次性使用。
     * </p>
     *
     * <h3>验证逻辑：</h3>
     * <ol>
     *   <li>从存储介质中获取对应键的验证码</li>
     *   <li>检查验证码是否存在且未过期</li>
     *   <li>比较用户输入与存储的验证码</li>
     *   <li>验证成功后清理或标记验证码</li>
     * </ol>
     *
     * <h3>安全考虑：</h3>
     * <ul>
     *   <li><strong>一次性使用</strong> - 验证成功后立即失效</li>
     *   <li><strong>时效性检查</strong> - 自动检查验证码是否过期</li>
     *   <li><strong>精确匹配</strong> - 严格比较验证码内容</li>
     * </ul>
     *
     * @param key 验证码标识符
     * @param code 用户输入的验证码
     * @return 验证结果，true表示验证成功，false表示验证失败
     */
    boolean verify(String key, T code);

    /**
     * 获取验证码信息
     * <p>
     * 从存储介质中获取指定键对应的完整验证码信息。
     * 返回的对象包含验证码内容、创建时间等详细信息。
     * </p>
     *
     * <h3>返回信息：</h3>
     * <ul>
     *   <li><strong>验证码内容</strong> - 实际的验证码值</li>
     *   <li><strong>创建时间</strong> - 验证码的生成时间戳</li>
     *   <li><strong>状态信息</strong> - 验证码的当前状态</li>
     * </ul>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li><strong>重发检查</strong> - 检查是否可以重新发送验证码</li>
     *   <li><strong>状态查询</strong> - 查询验证码的当前状态</li>
     *   <li><strong>调试诊断</strong> - 用于问题排查和调试</li>
     * </ul>
     *
     * @param key 验证码标识符
     * @return 验证码信息对象，如果不存在则返回null
     */
    SmsVerifyCode<T> get(String key);

    /**
     * 验证是否可以重新发送验证码
     * <p>
     * 检查指定键的验证码是否可以重新发送。
     * 通过比较上次发送时间和重发间隔来判断是否允许重新发送，防止验证码被频繁发送。
     * </p>
     *
     * <h3>检查逻辑：</h3>
     * <ol>
     *   <li>获取当前存储的验证码信息</li>
     *   <li>如果验证码不存在，允许发送</li>
     *   <li>如果验证码存在，检查时间间隔</li>
     *   <li>比较当前时间与上次发送时间加上间隔时间</li>
     * </ol>
     *
     * <h3>防护机制：</h3>
     * <ul>
     *   <li><strong>频率限制</strong> - 防止短时间内频繁发送</li>
     *   <li><strong>时间窗口</strong> - 基于配置的时间间隔控制</li>
     *   <li><strong>自动判断</strong> - 无需手动计算时间差</li>
     * </ul>
     *
     * <h3>默认实现：</h3>
     * <pre>{@code
     * SmsVerifyCode<T> code = get(key);
     * if (code == null) {
     *     return true; // 无验证码，可以发送
     * }
     * LocalDateTime created = LocalDateTimeUtil.of(code.getCreateTimeMillis());
     * return created.plusSeconds(getInterval()).isBefore(LocalDateTime.now());
     * }</pre>
     *
     * @param key 验证码标识符
     * @return true表示可以重新发送，false表示需要等待
     */
    default boolean verifyReSend(String key) {
        SmsVerifyCode<T> code = get(key);
        if (code == null) {
            return true;
        }
        LocalDateTime created = LocalDateTimeUtil.of(code.getCreateTimeMillis());
        return created.plusSeconds(getInterval()).isBefore(LocalDateTime.now());
    }

    /**
     * 获取验证码有效期
     * <p>
     * 返回验证码的有效时间长度，单位为秒。
     * 超过此时间的验证码将被视为过期，无法通过验证。
     * </p>
     *
     * <h3>用途：</h3>
     * <ul>
     *   <li><strong>过期控制</strong> - 控制验证码的生命周期</li>
     *   <li><strong>存储配置</strong> - 为存储介质设置TTL</li>
     *   <li><strong>安全保障</strong> - 限制验证码的有效时间窗口</li>
     * </ul>
     *
     * <h3>默认值：</h3>
     * <ul>
     *   <li><strong>时长</strong> - 300秒（5分钟）</li>
     *   <li><strong>单位</strong> - 秒</li>
     *   <li><strong>可重写</strong> - 实现类可以重写此方法</li>
     * </ul>
     *
     * @return 验证码有效期（秒），默认300秒
     */
    default long getPeriod() {
        return 300;
    }

    /**
     * 获取验证码重发间隔
     * <p>
     * 返回两次验证码发送之间的最小时间间隔，单位为秒。
     * 用于防止验证码被恶意频繁发送，保护系统资源和用户体验。
     * </p>
     *
     * <h3>作用：</h3>
     * <ul>
     *   <li><strong>频率控制</strong> - 限制验证码发送频率</li>
     *   <li><strong>资源保护</strong> - 防止短信资源被滥用</li>
     *   <li><strong>用户体验</strong> - 避免用户收到过多验证码</li>
     * </ul>
     *
     * <h3>应用场景：</h3>
     * <ul>
     *   <li><strong>重发检查</strong> - verifyReSend()方法使用此值</li>
     *   <li><strong>前端控制</strong> - 前端倒计时显示</li>
     *   <li><strong>业务规则</strong> - 业务层面的发送控制</li>
     * </ul>
     *
     * <h3>默认值：</h3>
     * <ul>
     *   <li><strong>时长</strong> - 60秒（1分钟）</li>
     *   <li><strong>单位</strong> - 秒</li>
     *   <li><strong>可重写</strong> - 实现类可以重写此方法</li>
     * </ul>
     *
     * @return 验证码重发间隔（秒），默认60秒
     */
    default long getInterval() {
        return 60;
    }
}
