package com.jingfang.security.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * LoginMode
 *
 * @author jpjoo
 */
@Getter
public enum LoginMode {

    /**
     * 账号登陆
     */
    PWD("0", "账号登陆"),

    /**
     * 手机短信登陆
     */
    SMS("1", "手机短信登陆"),

    /**
     * 邮箱验证码登陆
     */
    MAIL("3", "邮箱验证码登陆");


    /**
     * 登陆方式唯一标识
     */
    final String code;

    /**
     * 登陆描述
     */
    final String desc;


    LoginMode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LoginMode get(String id) {
        return Arrays.stream(LoginMode.values()).filter(loginModeEnum -> loginModeEnum.code.equals(id))
                .findFirst().orElseThrow((Supplier<RuntimeException>) () -> new IllegalArgumentException("不支持的登陆方式"));
    }
}