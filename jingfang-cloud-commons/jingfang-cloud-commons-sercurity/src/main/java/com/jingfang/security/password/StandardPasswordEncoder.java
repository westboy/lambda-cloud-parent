package com.jingfang.security.password;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;

/**
 * Md5PasswordEncoder
 *
 * @author jpjoo
 */
public class StandardPasswordEncoder implements PasswordEncoder {
    private static final int MAXIMUM_ITERATION_SIZE = 2;

    private final PasswordEncoder passwordEncoder = new Sha256PasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return this.passwordEncoder.encode(md5Wrapper(rawPassword.toString()));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return this.passwordEncoder.matches(md5Wrapper(rawPassword.toString()), encodedPassword);
    }

    /***
     * 原文2次MD5加密
     * @param rawPassword 原密码
     * @return
     */
    private String md5Wrapper(String rawPassword) {
        String encodedPassword = rawPassword;
        for (int i = 0; i < MAXIMUM_ITERATION_SIZE; i++) {
            encodedPassword = DigestUtils.md5Hex(encodedPassword.getBytes(StandardCharsets.UTF_8));
        }
        return encodedPassword;
    }

    public static void main(String[] args) {
        StandardPasswordEncoder standardPasswordEncoder = new StandardPasswordEncoder();
        System.out.println(standardPasswordEncoder.encode("123456"));
        System.out.println(standardPasswordEncoder.matches("123456","dd8b4d24b2aff492b1c893894af1f54fe610435d8152678697e055307029e509ebd6ff9dfb1e1e71"));
    }
}