package com.lambda.security.encode;

import static org.springframework.security.crypto.util.EncodingUtils.concatenate;
import static org.springframework.security.crypto.util.EncodingUtils.subArray;

import java.security.MessageDigest;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SHA-256密码编码器
 * <p>
 * 实现Spring Security的PasswordEncoder接口，提供基于SHA-256算法的密码编码和验证功能。
 * 使用SHA-256哈希算法结合盐值和可选的密钥，为密码提供安全的编码保护。
 * </p>
 *
 * <p><strong>注意：</strong>此编码器主要用于遗留系统兼容，现代应用建议使用更安全的编码器如BCrypt。</p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>SHA-256哈希算法编码</li>
 *   <li>随机盐值生成和使用</li>
 *   <li>可选密钥增强安全性</li>
 *   <li>多次迭代哈希计算</li>
 * </ul>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li>使用安全随机数生成器产生盐值</li>
 *   <li>支持密钥混合，增强安全性</li>
 *   <li>默认1024次迭代，防止暴力破解</li>
 *   <li>十六进制编码输出，便于存储</li>
 * </ul>
 *
 * <h3>编码格式：</h3>
 * <p>编码后的密码格式为：盐值 + 哈希值（十六进制编码）</p>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * Sha256PasswordEncoder encoder = new Sha256PasswordEncoder();
 * String encoded = encoder.encode("password");
 * boolean matches = encoder.matches("password", encoded);
 * }</pre>
 *
 * @author jpjoo
 * @since 1.0.0
 * @see PasswordEncoder
 * @see Digester
 */
public final class Sha256PasswordEncoder implements PasswordEncoder {

    /**
     * 消息摘要器
     * <p>
     * 用于执行SHA-256哈希计算的摘要器实例，
     * 配置了默认的迭代次数以增强安全性。
     * </p>
     */
    private final Digester digester;

    /**
     * 密钥字节数组
     * <p>
     * 可选的密钥，用于增强密码编码的安全性。
     * 密钥会与盐值和密码一起参与哈希计算。
     * </p>
     */
    private final byte[] secret;

    /**
     * 盐值生成器
     * <p>
     * 用于生成随机盐值的安全随机数生成器。
     * 每次编码都会生成新的盐值，防止彩虹表攻击。
     * </p>
     */
    private final BytesKeyGenerator saltGenerator;

    /**
     * 构造标准密码编码器（无密钥）
     * <p>
     * 创建一个不使用额外密钥的SHA-256密码编码器。
     * 仅使用盐值和密码进行哈希计算。
     * </p>
     */
    public Sha256PasswordEncoder() {
        this("");
    }

    /**
     * 构造带密钥的密码编码器
     * <p>
     * 创建一个使用额外密钥的SHA-256密码编码器。
     * 密钥会与盐值和密码一起参与哈希计算，提供额外的安全保护。
     * </p>
     *
     * @param secret 用于编码过程的密钥（应保密，不可共享）
     */
    public Sha256PasswordEncoder(CharSequence secret) {
        this("SHA-256", secret);
    }

    /**
     * 编码原始密码
     * <p>
     * 使用随机生成的盐值对原始密码进行SHA-256编码。
     * 每次调用都会生成新的盐值，确保相同密码产生不同的编码结果。
     * </p>
     *
     * @param rawPassword 原始密码
     * @return 编码后的密码（十六进制格式）
     */
    @Override
    public String encode(CharSequence rawPassword) {
        return encode(rawPassword, saltGenerator.generateKey());
    }

    /**
     * 验证原始密码与编码密码是否匹配
     * <p>
     * 从编码密码中提取盐值，使用相同的算法对原始密码进行编码，
     * 然后比较结果是否一致。使用MessageDigest.isEqual进行安全比较。
     * </p>
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 已编码的密码
     * @return 如果密码匹配返回true，否则返回false
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        byte[] digested = decode(encodedPassword);
        byte[] salt = subArray(digested, 0, saltGenerator.getKeyLength());
        return MessageDigest.isEqual(digested, digest(rawPassword, salt));
    }

    // internal helpers

    /**
     * 内部构造方法
     * <p>
     * 使用指定的算法和密钥创建密码编码器实例。
     * 初始化摘要器、密钥和盐值生成器。
     * </p>
     *
     * @param algorithm 哈希算法名称
     * @param secret 密钥字符序列
     */
    private Sha256PasswordEncoder(String algorithm, CharSequence secret) {
        this.digester = new Digester(algorithm, DEFAULT_ITERATIONS);
        this.secret = Utf8.encode(secret);
        this.saltGenerator = KeyGenerators.secureRandom();
    }

    /**
     * 使用指定盐值编码密码
     * <p>
     * 内部方法，使用给定的盐值对密码进行编码，
     * 返回十六进制格式的编码结果。
     * </p>
     *
     * @param rawPassword 原始密码
     * @param salt 盐值字节数组
     * @return 十六进制编码的密码
     */
    private String encode(CharSequence rawPassword, byte[] salt) {
        byte[] digest = digest(rawPassword, salt);
        return new String(Hex.encode(digest));
    }

    /**
     * 计算密码摘要
     * <p>
     * 将盐值、密钥和密码连接后进行哈希计算，
     * 然后将盐值与哈希结果连接返回。
     * </p>
     *
     * @param rawPassword 原始密码
     * @param salt 盐值字节数组
     * @return 盐值+哈希值的字节数组
     */
    private byte[] digest(CharSequence rawPassword, byte[] salt) {
        byte[] digest = digester.digest(concatenate(salt, secret, Utf8.encode(rawPassword)));
        return concatenate(salt, digest);
    }

    /**
     * 解码十六进制密码
     * <p>
     * 将十六进制格式的编码密码转换为字节数组。
     * </p>
     *
     * @param encodedPassword 十六进制编码的密码
     * @return 解码后的字节数组
     */
    private byte[] decode(CharSequence encodedPassword) {
        return Hex.decode(encodedPassword);
    }

    /**
     * 默认迭代次数
     * <p>
     * SHA-256哈希算法的默认迭代次数，用于增强安全性。
     * 较高的迭代次数可以有效防止暴力破解攻击。
     * </p>
     */
    private static final int DEFAULT_ITERATIONS = 1024;
}
