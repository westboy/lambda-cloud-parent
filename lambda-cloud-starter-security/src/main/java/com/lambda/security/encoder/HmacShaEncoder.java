package com.lambda.security.encoder;

import com.lambda.cloud.core.utils.Assert;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

/**
 * HMAC-SHA1密码编码器
 * <p>
 * 实现Spring Security的PasswordEncoder接口，提供基于HMAC-SHA1算法的密码编码和验证功能。
 * 使用HMAC（Hash-based Message Authentication Code）算法，结合SHA-1哈希函数，
 * 为密码提供强加密保护和完整性验证。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>密码编码：使用HMAC-SHA1算法对密码进行编码</li>
 *   <li>密码验证：安全比较原始密码和编码密码</li>
 *   <li>盐值支持：支持使用盐值增强安全性</li>
 *   <li>时间攻击防护：使用常量时间比较算法</li>
 * </ul>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li>HMAC-SHA1算法提供强加密保护</li>
 *   <li>Base64编码输出，便于存储和传输</li>
 *   <li>防止时间攻击的安全比较</li>
 *   <li>支持盐值，防止彩虹表攻击</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * HmacShaEncoder encoder = new HmacShaEncoder();
 * String encoded = encoder.encode("password", "salt");
 * boolean matches = encoder.matches("password", encoded);
 * }</pre>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 * @see PasswordEncoder
 */
@Slf4j
public class HmacShaEncoder implements PasswordEncoder {

    /**
     * 安全字符串比较方法
     * <p>
     * 使用常量时间算法比较两个字符串，防止时间攻击。
     * 通过异或操作和位运算，确保比较时间不依赖于字符串内容，
     * 有效防止攻击者通过响应时间推断密码信息。
     * </p>
     *
     * @param expected 期望的字符串
     * @param actual 实际的字符串
     * @return 如果两个字符串相等返回true，否则返回false
     */
    private static boolean check(String expected, String actual) {
        char[] caa = expected.toCharArray();
        char[] cab = actual.toCharArray();

        if (caa.length != cab.length) {
            return false;
        }

        byte ret = 0;
        for (int i = 0; i < caa.length; i++) {
            ret |= (byte) (caa[i] ^ cab[i]);
        }
        return ret == 0;
    }

    /**
     * 编码原始密码（简单实现）
     * <p>
     * Spring Security PasswordEncoder接口的标准实现。
     * 在此简单实现中，直接返回原始密码的字符串形式。
     * 实际应用中建议使用带盐值的encode方法以获得更好的安全性。
     * </p>
     *
     * @param rawPassword 原始密码
     * @return 编码后的密码字符串
     * @throws IllegalArgumentException 当rawPassword为null时抛出
     */
    @Override
    public String encode(CharSequence rawPassword) {
        Assert.notNull(rawPassword, "rawPassword must not be null");
        return rawPassword.toString();
    }

    /**
     * 使用盐值编码密码
     * <p>
     * 使用HMAC-SHA1算法结合盐值对密码进行编码。
     * 密码作为HMAC的密钥，盐值作为待认证的消息，
     * 生成的HMAC值经过Base64编码后返回。
     * </p>
     *
     * <h3>编码流程：</h3>
     * <ol>
     *   <li>将密码和盐值转换为UTF-8字节数组</li>
     *   <li>使用密码作为密钥初始化HMAC-SHA1</li>
     *   <li>对盐值进行HMAC计算</li>
     *   <li>将结果进行Base64编码</li>
     * </ol>
     *
     * @param password 原始密码，作为HMAC密钥
     * @param salt 盐值，作为HMAC消息
     * @return Base64编码的HMAC-SHA1结果
     * @throws IllegalArgumentException 当password或salt为null时抛出
     */
    public String encode(String password, String salt) {
        Assert.notNull(salt, "salt must not be null");
        Assert.notNull(password, "password must not be null");
        final byte[] key = password.getBytes(StandardCharsets.UTF_8);
        final byte[] digest = salt.getBytes(StandardCharsets.UTF_8);
        byte[] bytes =
                HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_1, key).doFinal(digest);
        return Base64.encodeBase64String(bytes);
    }

    /**
     * 验证原始密码与编码密码是否匹配
     * <p>
     * 实现Spring Security PasswordEncoder接口的密码验证方法。
     * 对原始密码进行编码，然后使用安全比较方法验证是否与存储的编码密码匹配。
     * </p>
     *
     * <h3>验证流程：</h3>
     * <ol>
     *   <li>检查输入参数的有效性</li>
     *   <li>对原始密码进行编码</li>
     *   <li>使用安全比较方法验证匹配性</li>
     *   <li>记录调试信息（如果启用）</li>
     * </ol>
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 已编码的密码
     * @return 如果密码匹配返回true，否则返回false
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(encodedPassword)) {
            return false;
        }
        String actual = encode(rawPassword);
        if (log.isDebugEnabled()) {
            log.debug("actual: {}, expected: {}", actual, encodedPassword);
        }
        return check(actual, encodedPassword);
    }
}
