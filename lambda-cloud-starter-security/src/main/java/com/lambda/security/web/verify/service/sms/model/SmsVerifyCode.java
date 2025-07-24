package com.lambda.security.web.verify.service.sms.model;

import lombok.Getter;

/**
 * 短信验证码数据模型
 *
 * <p>封装短信验证码的核心信息，包括验证码内容和创建时间戳。
 * 通过泛型设计支持多种类型的验证码，如数字、字符串等。
 *
 * <p>设计目标：
 * <ul>
 *   <li>类型安全：通过泛型支持不同类型的验证码</li>
 *   <li>时间追踪：记录验证码的创建时间用于过期判断</li>
 *   <li>不可变性：所有字段都是final的，确保数据一致性</li>
 *   <li>轻量级：简单的数据结构，性能开销最小</li>
 * </ul>
 *
 * <p>主要功能：
 * <ul>
 *   <li>验证码存储：保存验证码的实际内容</li>
 *   <li>时间戳记录：记录验证码的创建时间</li>
 *   <li>过期判断：配合时间戳进行有效期验证</li>
 *   <li>类型适配：支持不同类型的验证码内容</li>
 * </ul>
 *
 * <p>支持的验证码类型：
 * <ul>
 *   <li>数字验证码：Integer类型，如123456</li>
 *   <li>字符串验证码：String类型，如"ABC123"</li>
 *   <li>自定义类型：任何实现了合适equals和hashCode的类型</li>
 * </ul>
 *
 * <p>时间戳特性：
 * <ul>
 *   <li>毫秒精度：使用System.currentTimeMillis()获取</li>
 *   <li>UTC时间：基于系统时间，不受时区影响</li>
 *   <li>单调递增：保证时间戳的顺序性</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 数字验证码
 * SmsVerifyCode<Integer> numCode = new SmsVerifyCode<>(123456);
 * Integer code = numCode.getCode();
 * long createTime = numCode.getCreateTimeMillis();
 *
 * // 字符串验证码
 * SmsVerifyCode<String> strCode = new SmsVerifyCode<>("ABC123");
 * String codeStr = strCode.getCode();
 *
 * // 过期检查
 * long now = System.currentTimeMillis();
 * long expireTime = 5 * 60 * 1000; // 5分钟
 * boolean isExpired = (now - createTime) > expireTime;
 * }</pre>
 *
 * <p>存储建议：
 * <ul>
 *   <li>Redis存储：适合分布式环境下的验证码管理</li>
 *   <li>内存存储：适合单机环境下的临时验证码</li>
 *   <li>数据库存储：适合需要持久化的验证码场景</li>
 * </ul>
 *
 * <p>安全考虑：
 * <ul>
 *   <li>验证码强度：建议使用足够长度的随机验证码</li>
 *   <li>有效期控制：通过时间戳实现验证码的自动过期</li>
 *   <li>一次性使用：验证成功后应立即删除验证码</li>
 * </ul>
 *
 * @param <T> 验证码的数据类型，如Integer、String等
 * @author Jin
 * @see SmsVerifyCodeResponse
 * @see SmsVerifyCodeStore
 */
@Getter
public class SmsVerifyCode<T> {

    /**
     * 验证码内容
     *
     * <p>存储实际的验证码数据，支持泛型类型以适应不同的验证码格式。
     * 验证码内容在创建后不可修改，确保数据的一致性和安全性。
     *
     * <p>内容特性：
     * <ul>
     *   <li>类型灵活：支持Integer、String等多种类型</li>
     *   <li>不可变：final修饰，创建后无法修改</li>
     *   <li>非空约束：通常不应为null，由构造函数保证</li>
     * </ul>
     *
     * <p>常见格式：
     * <ul>
     *   <li>6位数字：123456（Integer类型）</li>
     *   <li>4位数字：1234（Integer类型）</li>
     *   <li>字母数字：ABC123（String类型）</li>
     * </ul>
     */
    private final T code;

    /**
     * 验证码创建时间戳（毫秒）
     *
     * <p>记录验证码创建的精确时间，用于计算验证码的有效期和过期状态。
     * 使用System.currentTimeMillis()获取，提供毫秒级的时间精度。
     *
     * <p>时间戳用途：
     * <ul>
     *   <li>过期判断：与当前时间比较确定是否过期</li>
     *   <li>有效期计算：结合配置的有效期进行验证</li>
     *   <li>审计日志：记录验证码的生命周期</li>
     *   <li>统计分析：分析验证码的使用模式</li>
     * </ul>
     *
     * <p>时间精度：
     * <ul>
     *   <li>毫秒级：提供足够的时间精度</li>
     *   <li>UTC时间：基于系统时间，避免时区问题</li>
     *   <li>单调性：保证时间戳的递增特性</li>
     * </ul>
     */
    private final long createTimeMillis;

    /**
     * 构造短信验证码实例
     *
     * <p>创建一个新的短信验证码对象，自动记录当前时间作为创建时间戳。
     * 验证码内容在创建后不可修改，确保数据的完整性。
     *
     * <p>构造过程：
     * <ol>
     *   <li>保存验证码内容到code字段</li>
     *   <li>获取当前系统时间戳</li>
     *   <li>初始化createTimeMillis字段</li>
     * </ol>
     *
     * <p>参数要求：
     * <ul>
     *   <li>code不应为null：虽然编译器不强制，但业务逻辑上应避免</li>
     *   <li>code应有意义：应该是有效的验证码内容</li>
     *   <li>类型一致：T的实际类型应与使用场景匹配</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>{@code
     * // 创建数字验证码
     * SmsVerifyCode<Integer> code1 = new SmsVerifyCode<>(123456);
     *
     * // 创建字符串验证码
     * SmsVerifyCode<String> code2 = new SmsVerifyCode<>("ABC123");
     *
     * // 获取创建时间
     * long createTime = code1.getCreateTimeMillis();
     * }</pre>
     *
     * <p>最佳实践：
     * <ul>
     *   <li>验证码生成：使用安全的随机数生成器</li>
     *   <li>长度控制：根据安全要求选择合适的验证码长度</li>
     *   <li>类型选择：根据业务需求选择合适的泛型类型</li>
     * </ul>
     *
     * @param code 验证码内容，不应为null
     * @throws NullPointerException 如果code为null（取决于具体使用场景）
     */
    public SmsVerifyCode(T code) {
        this.code = code;
        this.createTimeMillis = System.currentTimeMillis();
    }
}
