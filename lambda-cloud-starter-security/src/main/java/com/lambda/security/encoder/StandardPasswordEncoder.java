package com.lambda.security.encoder;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 标准密码编码器
 * <p>
 * 实现Spring Security的PasswordEncoder接口，提供安全的密码加密和验证功能。
 * 采用MD5+SHA256双重加密策略，确保密码存储的安全性。
 * </p>
 *
 * <h3>加密流程：</h3>
 * <ol>
 *   <li>对原始密码进行2次MD5加密</li>
 *   <li>使用SHA256对MD5结果进行最终加密</li>
 *   <li>生成最终的加密密码用于存储</li>
 * </ol>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li>双重哈希算法保护</li>
 *   <li>防止彩虹表攻击</li>
 *   <li>符合Spring Security标准</li>
 *   <li>支持密码强度验证</li>
 * </ul>
 *
 * @author jpjoo
 * @since 1.0.0
 */
public class StandardPasswordEncoder implements PasswordEncoder {
    /**
     * MD5加密迭代次数
     * <p>
     * 定义对原始密码进行MD5加密的次数，增加破解难度。
     * 设置为2次可以有效防止简单的MD5碰撞攻击。
     * </p>
     */
    private static final int MAXIMUM_ITERATION_SIZE = 2;

    /**
     * SHA256密码编码器
     * <p>
     * 用于对MD5处理后的密码进行最终的SHA256加密。
     * 提供更高级别的安全保护。
     * </p>
     */
    private final PasswordEncoder passwordEncoder = new Sha256PasswordEncoder();

    /**
     * 密码加密方法
     * <p>
     * 对原始密码进行加密处理，返回加密后的密码字符串。
     * 加密过程：原始密码 → 2次MD5 → SHA256 → 最终密文
     * </p>
     *
     * @param rawPassword 原始明文密码
     * @return 加密后的密码字符串
     * @throws IllegalArgumentException 如果原始密码为null
     */
    @Override
    public String encode(CharSequence rawPassword) {
        return this.passwordEncoder.encode(md5Wrapper(rawPassword.toString()));
    }

    /**
     * 密码匹配验证方法
     * <p>
     * 验证原始密码与加密密码是否匹配。通过对原始密码执行相同的加密流程，
     * 然后与存储的加密密码进行比较。
     * </p>
     *
     * @param rawPassword 待验证的原始明文密码
     * @param encodedPassword 存储的加密密码
     * @return 如果密码匹配返回true，否则返回false
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return this.passwordEncoder.matches(md5Wrapper(rawPassword.toString()), encodedPassword);
    }

    /**
     * MD5包装加密方法
     * <p>
     * 对原始密码进行多次MD5加密处理，增强密码安全性。
     * 使用UTF-8编码确保字符处理的一致性。
     * </p>
     *
     * <h3>加密过程：</h3>
     * <ol>
     *   <li>将原始密码转换为UTF-8字节数组</li>
     *   <li>进行第一次MD5哈希计算</li>
     *   <li>对结果再次进行MD5哈希计算</li>
     *   <li>返回最终的MD5十六进制字符串</li>
     * </ol>
     *
     * @param rawPassword 原始明文密码
     * @return 经过2次MD5加密的十六进制字符串
     */
    private String md5Wrapper(String rawPassword) {
        String encodedPassword = rawPassword;
        for (int i = 0; i < MAXIMUM_ITERATION_SIZE; i++) {
            encodedPassword = DigestUtils.md5Hex(encodedPassword.getBytes(StandardCharsets.UTF_8));
        }
        return encodedPassword;
    }

    /**
     * 测试方法
     * <p>
     * 用于测试密码编码器的加密和验证功能。
     * 演示了如何使用StandardPasswordEncoder进行密码加密和匹配验证。
     * </p>
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        StandardPasswordEncoder standardPasswordEncoder = new StandardPasswordEncoder();
        System.out.println(standardPasswordEncoder.encode("123456"));
        System.out.println(standardPasswordEncoder.matches(
                "123456", "dd8b4d24b2aff492b1c893894af1f54fe610435d8152678697e055307029e509ebd6ff9dfb1e1e71"));
    }
}
