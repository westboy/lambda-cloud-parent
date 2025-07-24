package com.lambda.security;

import java.util.Arrays;
import java.util.function.Supplier;
import lombok.Getter;

/**
 * 登录方式枚举
 * <p>
 * 定义Lambda Cloud安全模块支持的所有登录方式。
 * 每种登录方式都有唯一的标识码和描述信息，便于系统识别和用户理解。
 * </p>
 *
 * <h3>支持的登录方式：</h3>
 * <ul>
 *   <li>账号密码登录：传统的用户名/密码认证方式</li>
 *   <li>短信验证码登录：通过手机号接收验证码进行登录</li>
 *   <li>邮箱验证码登录：通过邮箱接收验证码进行登录</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 获取指定登录方式
 * LoginMode mode = LoginMode.get("0");
 * System.out.println(mode.getDesc()); // 输出：账号登陆
 * }</pre>
 *
 * @author jpjoo
 * @since 1.0.0
 */
@Getter
public enum LoginMode {

    /**
     * 账号密码登录
     * <p>
     * 传统的用户名/密码认证方式，用户需要提供正确的用户名和密码。
     * 这是最常见和基础的登录方式，适用于大多数应用场景。
     * </p>
     */
    PWD("0", "账号登陆"),

    /**
     * 手机短信验证码登录
     * <p>
     * 通过向用户手机发送验证码进行身份验证的登录方式。
     * 用户输入手机号后，系统发送验证码短信，用户输入正确验证码即可登录。
     * 提供了更好的安全性和用户体验。
     * </p>
     */
    SMS("1", "手机短信登陆"),

    /**
     * 邮箱验证码登录
     * <p>
     * 通过向用户邮箱发送验证码进行身份验证的登录方式。
     * 用户输入邮箱地址后，系统发送验证码邮件，用户输入正确验证码即可登录。
     * 适用于企业用户或偏好邮箱验证的场景。
     * </p>
     */
    MAIL("3", "邮箱验证码登陆");

    /**
     * 登录方式唯一标识码
     * <p>
     * 用于在系统内部标识不同的登录方式，每个登录方式都有唯一的标识码。
     * 前端可以通过此标识码来指定用户选择的登录方式。
     * </p>
     */
    final String code;

    /**
     * 登录方式描述信息
     * <p>
     * 用于向用户展示的登录方式名称，便于用户理解和选择。
     * 通常在前端界面中作为选项文本显示。
     * </p>
     */
    final String desc;

    LoginMode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据标识码获取登录方式枚举
     * <p>
     * 通过登录方式的唯一标识码查找对应的枚举实例。
     * 如果找不到匹配的登录方式，将抛出IllegalArgumentException异常。
     * </p>
     *
     * @param id 登录方式标识码
     * @return 对应的登录方式枚举实例
     * @throws IllegalArgumentException 当传入的标识码不存在时抛出
     *
     * @since 1.0.0
     */
    public static LoginMode get(String id) {
        return Arrays.stream(LoginMode.values())
                .filter(loginModeEnum -> loginModeEnum.code.equals(id))
                .findFirst()
                .orElseThrow((Supplier<RuntimeException>) () -> new IllegalArgumentException("不支持的登陆方式"));
    }
}
