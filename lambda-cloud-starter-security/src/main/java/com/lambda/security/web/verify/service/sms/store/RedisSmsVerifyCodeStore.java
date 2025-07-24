package com.lambda.security.web.verify.service.sms.store;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.security.web.verify.service.sms.model.SmsVerifyCode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;

/**
 * 基于Redis的短信验证码存储实现
 * <p>
 * 提供基于Redis的短信验证码存储和管理服务，支持验证码的生成、存储、验证和过期管理。
 * 该实现利用Redis的过期机制自动清理过期的验证码，提供高性能的分布式验证码存储解决方案。
 * </p>
 *
 * <h3>设计目标：</h3>
 * <ul>
 *   <li><strong>分布式支持</strong> - 基于Redis实现分布式验证码存储</li>
 *   <li><strong>自动过期</strong> - 利用Redis TTL机制自动清理过期验证码</li>
 *   <li><strong>高性能</strong> - Redis内存存储提供快速的读写性能</li>
 *   <li><strong>线程安全</strong> - Redis操作天然支持并发访问</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>验证码生成</strong> - 生成4位数字验证码</li>
 *   <li><strong>Redis存储</strong> - 将验证码存储到Redis中</li>
 *   <li><strong>过期管理</strong> - 自动设置验证码过期时间</li>
 *   <li><strong>验证码验证</strong> - 验证用户输入的验证码</li>
 *   <li><strong>一次性使用</strong> - 验证成功后自动删除验证码</li>
 * </ul>
 *
 * <h3>存储结构：</h3>
 * <ul>
 *   <li><strong>键格式</strong> - "Authorization:login:smsVerify:{key}"</li>
 *   <li><strong>值格式</strong> - JSON序列化的SmsVerifyCode对象</li>
 *   <li><strong>过期时间</strong> - 根据配置的有效分钟数设置TTL</li>
 * </ul>
 *
 * <h3>验证码特性：</h3>
 * <ul>
 *   <li><strong>格式</strong> - 4位数字验证码（1000-9999）</li>
 *   <li><strong>有效期</strong> - 可配置的分钟数</li>
 *   <li><strong>重发间隔</strong> - 可配置的秒数间隔</li>
 *   <li><strong>一次性</strong> - 验证成功后立即失效</li>
 * </ul>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li><strong>时效性</strong> - 验证码具有明确的过期时间</li>
 *   <li><strong>一次性使用</strong> - 防止验证码重复使用</li>
 *   <li><strong>随机生成</strong> - 使用安全的随机数生成器</li>
 *   <li><strong>自动清理</strong> - 过期验证码自动删除</li>
 * </ul>
 *
 * <h3>配置依赖：</h3>
 * <ul>
 *   <li><strong>有效时间</strong> - smsLogin.validMinutes</li>
 *   <li><strong>重发间隔</strong> - smsLogin.resendSeconds</li>
 *   <li><strong>Redis连接</strong> - StringRedisTemplate</li>
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
 * // 获取验证码信息
 * SmsVerifyCode<String> verifyCode = store.get("13800138000");
 * }</pre>
 *
 * @author jpjoo
 * @see SmsVerifyCodeStore
 * @see SmsVerifyCode
 * @see SecurityProperties.SmsLogin
 * @since 1.0.0
 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP2"},
        justification = "springboot properties")
public class RedisSmsVerifyCodeStore implements SmsVerifyCodeStore<String> {

    /**
     * JSON序列化工具
     * <p>
     * 用于将SmsVerifyCode对象序列化为JSON字符串存储到Redis中，
     * 以及从Redis中反序列化JSON字符串为SmsVerifyCode对象。
     * </p>
     */
    private final Gson gson = new Gson();

    /**
     * Redis键前缀
     * <p>
     * 用于构建短信验证码在Redis中的存储键，格式为：
     * "Authorization:login:smsVerify:{key}"，其中{key}为具体的标识符（如手机号）。
     * </p>
     *
     * <h3>键命名规范：</h3>
     * <ul>
     *   <li><strong>业务标识</strong> - Authorization表示认证相关</li>
     *   <li><strong>功能标识</strong> - login表示登录功能</li>
     *   <li><strong>类型标识</strong> - smsVerify表示短信验证</li>
     * </ul>
     */
    private static final String PREFIX_KEY = "Authorization:login:smsVerify";

    /**
     * 短信登录配置
     * <p>
     * 包含短信验证码相关的配置信息，如有效时间、重发间隔等。
     * 用于控制验证码的生命周期和发送频率。
     * </p>
     */
    private final SecurityProperties.SmsLogin smsLogin;

    /**
     * Redis字符串操作模板
     * <p>
     * 用于执行Redis的字符串操作，包括存储、获取、删除验证码等。
     * 提供对Redis数据库的访问能力。
     * </p>
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造函数
     * <p>
     * 初始化Redis短信验证码存储服务，注入必要的依赖。
     * </p>
     *
     * <h3>依赖注入：</h3>
     * <ul>
     *   <li><strong>短信配置</strong> - 获取验证码有效期和重发间隔配置</li>
     *   <li><strong>Redis模板</strong> - 获取Redis操作能力</li>
     * </ul>
     *
     * @param smsLogin 短信登录配置，包含验证码相关参数
     * @param stringRedisTemplate Redis字符串操作模板
     */
    public RedisSmsVerifyCodeStore(SecurityProperties.SmsLogin smsLogin, StringRedisTemplate stringRedisTemplate) {
        this.smsLogin = smsLogin;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 获取验证码有效期
     * <p>
     * 返回短信验证码的有效时间，单位为分钟。
     * 该值从配置文件中读取，用于设置Redis中验证码的TTL。
     * </p>
     *
     * @return 验证码有效期（分钟）
     */
    @Override
    public long getPeriod() {
        return smsLogin.getValidMinutes();
    }

    /**
     * 获取验证码重发间隔
     * <p>
     * 返回短信验证码的重发间隔时间，单位为秒。
     * 该值从配置文件中读取，用于控制验证码的发送频率。
     * </p>
     *
     * @return 验证码重发间隔（秒）
     */
    @Override
    public long getInterval() {
        return smsLogin.getResendSeconds();
    }

    /**
     * 生成并存储验证码
     * <p>
     * 生成一个4位数字验证码，并将其存储到Redis中。
     * 验证码会自动设置过期时间，过期后自动删除。
     * </p>
     *
     * <h3>生成规则：</h3>
     * <ul>
     *   <li><strong>格式</strong> - 4位数字（1000-9999）</li>
     *   <li><strong>随机性</strong> - 使用RandomUtil生成随机数</li>
     *   <li><strong>唯一性</strong> - 每次生成都是新的验证码</li>
     * </ul>
     *
     * <h3>存储机制：</h3>
     * <ul>
     *   <li><strong>键格式</strong> - PREFIX_KEY + ":" + key</li>
     *   <li><strong>值格式</strong> - JSON序列化的SmsVerifyCode对象</li>
     *   <li><strong>过期时间</strong> - 根据配置的有效分钟数设置</li>
     * </ul>
     *
     * @param key 验证码标识符，通常为手机号或用户标识
     * @return 生成的4位数字验证码
     */
    @Override
    public String generate(String key) {
        String code = Integer.toString(RandomUtil.randomInt(1000, 9999));
        stringRedisTemplate
                .opsForValue()
                .set(getCommonKey(key), gson.toJson(new SmsVerifyCode<>(code)), getPeriod(), TimeUnit.MINUTES);
        return code;
    }

    /**
     * 验证验证码
     * <p>
     * 验证用户输入的验证码是否正确。如果验证成功，会立即删除Redis中的验证码，
     * 确保验证码只能使用一次。
     * </p>
     *
     * <h3>验证流程：</h3>
     * <ol>
     *   <li>从Redis中获取存储的验证码</li>
     *   <li>比较用户输入的验证码与存储的验证码</li>
     *   <li>如果匹配成功，删除Redis中的验证码</li>
     *   <li>返回验证结果</li>
     * </ol>
     *
     * <h3>安全特性：</h3>
     * <ul>
     *   <li><strong>一次性使用</strong> - 验证成功后立即删除</li>
     *   <li><strong>精确匹配</strong> - 使用equals进行严格比较</li>
     *   <li><strong>自动过期</strong> - 超时验证码自动失效</li>
     * </ul>
     *
     * @param key 验证码标识符，必须非空
     * @param code 用户输入的验证码，必须非空
     * @return 验证结果，true表示验证成功，false表示验证失败
     */
    @Override
    public boolean verify(@NonNull String key, @NonNull String code) {
        SmsVerifyCode<String> smsVerifyCode = get(key);
        boolean result = code.equals(smsVerifyCode.getCode());
        if (result) {
            stringRedisTemplate.delete(getCommonKey(key));
        }
        return result;
    }

    /**
     * 获取验证码信息
     * <p>
     * 从Redis中获取指定键对应的验证码信息。
     * 如果验证码不存在或已过期，返回null。
     * </p>
     *
     * <h3>获取逻辑：</h3>
     * <ol>
     *   <li>根据键从Redis中获取JSON字符串</li>
     *   <li>检查获取的内容是否为空</li>
     *   <li>如果不为空，反序列化为SmsVerifyCode对象</li>
     *   <li>返回验证码对象或null</li>
     * </ol>
     *
     * <h3>返回情况：</h3>
     * <ul>
     *   <li><strong>正常情况</strong> - 返回包含验证码的SmsVerifyCode对象</li>
     *   <li><strong>不存在</strong> - 验证码不存在时返回null</li>
     *   <li><strong>已过期</strong> - 验证码过期时返回null</li>
     * </ul>
     *
     * @param key 验证码标识符
     * @return 验证码信息对象，如果不存在则返回null
     */
    @Override
    public SmsVerifyCode<String> get(String key) {
        String cache = stringRedisTemplate.opsForValue().get(getCommonKey(key));
        if (StrUtil.isEmpty(cache)) {
            return null;
        }
        return gson.fromJson(cache, new SmsVerifyCodeTypeToken().getType());
    }

    /**
     * 构建Redis存储键
     * <p>
     * 根据业务键构建完整的Redis存储键。
     * 使用统一的键格式确保数据的组织性和可维护性。
     * </p>
     *
     * <h3>键格式：</h3>
     * <pre>{@code
     * Authorization:login:smsVerify:{key}
     * }</pre>
     *
     * <h3>设计考虑：</h3>
     * <ul>
     *   <li><strong>命名空间</strong> - 使用前缀避免键冲突</li>
     *   <li><strong>可读性</strong> - 键名具有明确的业务含义</li>
     *   <li><strong>层次结构</strong> - 使用冒号分隔不同层级</li>
     * </ul>
     *
     * @param key 业务标识符
     * @return 完整的Redis存储键
     */
    private String getCommonKey(String key) {
        return MessageFormat.format("{0}:{1}", PREFIX_KEY, key);
    }

    /**
     * 短信验证码类型令牌
     * <p>
     * 用于Gson反序列化时的类型信息保持。
     * 由于Java的类型擦除机制，泛型信息在运行时会丢失，
     * 通过继承TypeToken可以保持完整的泛型类型信息。
     * </p>
     *
     * <h3>作用：</h3>
     * <ul>
     *   <li><strong>类型安全</strong> - 确保反序列化时的类型正确性</li>
     *   <li><strong>泛型支持</strong> - 支持SmsVerifyCode&lt;String&gt;的完整类型信息</li>
     *   <li><strong>编译时检查</strong> - 提供编译时的类型检查</li>
     * </ul>
     *
     * @see TypeToken
     * @see SmsVerifyCode
     */
    private static class SmsVerifyCodeTypeToken extends TypeToken<SmsVerifyCode<String>> {}
}
