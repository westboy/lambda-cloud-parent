package com.jingfang.security.enums;

import cn.dev33.satoken.stp.StpLogic;
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
     * 管理员
     */
    ADMIN("admin", "管理员登录", new StpLogic("admin")),

    /**
     * 用户
     */
    USER("user", "用户登录", new StpLogic("user"));

    final String code;

    final String desc;

    final StpLogic stpLogic;


    LoginType(String code, String desc, StpLogic stpLogic) {
        this.code = code;
        this.desc = desc;
        this.stpLogic = stpLogic;
    }

    public static LoginType get(String id) {
        return Arrays.stream(LoginType.values()).filter(loginModeEnum -> loginModeEnum.code.equals(id))
                .findFirst().orElseThrow((Supplier<RuntimeException>) () -> new IllegalArgumentException("不支持的登陆方式"));
    }

    public static StpLogic getStpLogic(String id) {
        return get(id).getStpLogic();
    }


}