package com.jingfang.security.web;

import lombok.Getter;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * LoginType
 *
 * @author jpjoo
 */
@Getter
public enum LoginType {

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
    final String id;

    /**
     * 登陆描述
     */
    final String desc;


    LoginType(String id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public static LoginType getById(String id) {
        return Arrays.stream(LoginType.values()).filter(loginTypeEnum -> loginTypeEnum.id.equals(id))
                .findFirst().orElseThrow((Supplier<RuntimeException>) () -> new IllegalArgumentException("不支持的登陆方式"));
    }

    public static LoginType getByNameOrId(String condition) {
        return Arrays.stream(LoginType.values())
                .filter(loginType -> loginType.id.equals(condition) || loginType.name().equalsIgnoreCase(condition))
                .findFirst().orElseThrow((Supplier<RuntimeException>) () -> new IllegalArgumentException("不支持的登陆方式"));
    }


}