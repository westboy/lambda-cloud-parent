package com.lambda.cloud.core;

import com.google.gson.Gson;
import com.lambda.cloud.core.principal.AnonymousUser;
import com.lambda.cloud.core.principal.LoginUser;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

/**
 * 系统常量定义类
 * <p>
 * 该类定义了系统中使用的各种常量，包括：
 * <ul>
 *     <li>认证相关常量：HMAC、登录用户、登录类型等</li>
 *     <li>JSON处理常量：Gson实例</li>
 *     <li>日期时间格式常量：各种日期时间格式和正则表达式</li>
 *     <li>时间戳相关常量：时间戳格式和正则表达式</li>
 * </ul>
 *
 * <p>使用 {@link UtilityClass} 注解确保该类不能被实例化，所有常量均为静态常量。
 *
 * @author w
 * @since 1.0.0
 */
@UtilityClass
public final class Constants {

    // ==================== 认证相关常量 ====================

    /**
     * HMAC认证标识
     */
    public static final String HMAC = "hmac";

    /**
     * 登录用户标识
     */
    public static final String LOGIN_USER = "loginUser";

    /**
     * 登录类型标识
     */
    public static final String LOGIN_TYPE = "loginType";

    /**
     * 登录设备标识，用于标识用户登录的终端类型。
     * <p>例如 web、mobile、miniApp 等。</p>
     */
    public static final String LOGIN_DEVICE = "loginDevice";

    /**
     * 默认游客用户/匿名用户。
     *
     * <p>当用户未登录或获取用户信息失败时返回该对象，
     * 权限最小，所有敏感操作被限制，账户锁定且过期。</p>
     *
     * <h3>特征：</h3>
     * <ul>
     *   <li>用户名：anonymous</li>
     *   <li>账户状态：已锁定且已过期</li>
     *   <li>组织ID：anonymous</li>
     *   <li>租户ID：-1（无效租户）</li>
     * </ul>
     */
    public static final LoginUser ANONYMOUS_USER = new AnonymousUser();

    // ==================== JSON处理常量 ====================

    /**
     * Gson实例，用于JSON序列化和反序列化
     */
    public static final Gson GSON = new Gson();

    // ==================== 日期时间格式常量 ====================

    /**
     * 标准日期时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 标准日期格式：yyyy-MM-dd
     */
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * 标准时间格式：HH:mm:ss
     */
    public static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 年月格式：yyyy-MM
     */
    public static final String YEAR_MONTH_PATTERN = "yyyy-MM";

    /**
     * ISO8601格式：yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    public static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * ISO日期时间格式（带T分隔符）
     */
    public static final String ISO_DATE_TIME_PATTERN = DATE_TIME_PATTERN + "T " + TIME_PATTERN;

    // ==================== 日期时间正则表达式常量 ====================

    /**
     * 日期正则表达式，匹配格式：yyyy-MM-dd
     */
    public static final String DATE_REGEX = "[1-9]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])";

    /**
     * 时间正则表达式，匹配格式：HH:mm:ss
     */
    public static final String TIME_REGEX = "(20|21|22|23|[0-1]\\d):[0-5]\\d:[0-5]\\d";

    /**
     * 日期时间正则表达式，匹配格式：yyyy-MM-dd HH:mm:ss
     */
    public static final String DATE_TIME_REGEX = DATE_REGEX + "\\s" + TIME_REGEX;

    /**
     * 年月正则表达式，匹配格式：yyyy-MM
     */
    public static final String YEAR_MONTH_REGEX = "[1-9]\\d{3}-(0[1-9]|1[0-2])";

    /**
     * ISO8601正则表达式，匹配格式：yyyy-MM-ddTHH:mm:ss.SSSZ
     */
    public static final String ISO8601_REGEX = DATE_REGEX + "T" + TIME_REGEX + "\\.\\d{3}Z";

    /**
     * 13位时间戳正则表达式，匹配毫秒级时间戳
     */
    public static final String TIME_STAMP_REGEX = "1\\d{12}";

    // ==================== DateTimeFormatter常量 ====================

    /**
     * 日期格式化器：yyyy-MM-dd
     */
    public static final DateTimeFormatter YYYY_MM_DD_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    /**
     * 日期时间格式化器：yyyy-MM-dd HH:mm:ss
     */
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    // ========== 分页相关常量 ==========
    /** 页码不能为空的错误消息 */
    public static final String MSG_PAGE_NUM_NOT_NULL = "pageNum不能为空";

    /** 页面大小不能为空的错误消息 */
    public static final String MSG_PAGE_SIZE_NOT_NULL = "pageSize不能为空";

    // ========== Websocket 相关常量 ==========
    public static final String SYSTEM = "system";

    public static final String IP_ADDRESS = "ip";

    public static final String SIMPE_CONNECT_MESSAGE = "simpConnectMessage";

    public static final String X_WEBSOCKET_FRAMEWORK = "X-Websocket-Framework";

    public static final String COLON = ":";
}
