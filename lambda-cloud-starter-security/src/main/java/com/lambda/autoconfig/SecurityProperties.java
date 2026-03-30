package com.lambda.autoconfig;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lambda.cloud.core.shared.KeyValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Lambda Cloud 安全模块配置属性类
 * <p>
 * 该类定义了Lambda Cloud安全模块的所有配置属性，包括Sa-Token配置、
 * 各种认证方式的配置（表单登录、短信登录、HMAC认证、第三方登录）、
 * 验证码配置、XSS防护配置等。
 * </p>
 *
 * <h3>主要配置模块：</h3>
 * <ul>
 *   <li><strong>Sa-Token配置</strong> - 基础认证框架配置</li>
 *   <li><strong>HMAC认证</strong> - API签名认证配置</li>
 *   <li><strong>表单登录</strong> - 传统用户名密码登录配置</li>
 *   <li><strong>短信登录</strong> - 手机号验证码登录配置</li>
 *   <li><strong>第三方登录</strong> - 微信等第三方平台登录配置</li>
 *   <li><strong>验证码</strong> - 图形验证码防护配置</li>
 *   <li><strong>XSS防护</strong> - 跨站脚本攻击防护配置</li>
 * </ul>
 *
 * <h3>配置前缀：</h3>
 * <pre>{@code lambda.security}</pre>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * # application.yml
 * lambda:
 *   security:
 *     sa-token:
 *       token-name: "Authorization"
 *       timeout: 2592000
 *     form:
 *       enabled: true
 *       login-page: "/login"
 *     sms:
 *       enabled: true
 *       valid-minutes: 5
 * }</pre>
 *
 * @author jpjoo
 * @since 1.0.0
 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP"},
        justification = "springboot properties")
@Data
@ConfigurationProperties(prefix = "lambda.security")
public class SecurityProperties {

    private static final List<String> DEFAULT_IGNORE_PATH_LIST =
            Lists.newArrayList("/public/**", "/v3/**", "/anon/**", "*.html", "*.css", "*.ico", "*.js");

    /**
     * Sa-Token配置
     */
    @NestedConfigurationProperty
    ExtendSaTokenConfig saToken = new ExtendSaTokenConfig();

    /**
     * 扩展的Sa-Token配置类
     * <p>
     * 继承Sa-Token的基础配置，并添加了Lambda Cloud特有的配置项。
     * 提供了方法级认证开关和忽略路径配置等扩展功能。
     * </p>
     *
     * <h3>扩展功能：</h3>
     * <ul>
     *   <li>方法级认证开关控制</li>
     *   <li>忽略路径列表管理</li>
     *   <li>默认忽略路径预设</li>
     * </ul>
     */
    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "springboot properties")
    @Getter
    @Setter
    public static class ExtendSaTokenConfig extends SaTokenConfig {

        /**
         * 忽略拦截的路径列表
         * <p>
         * 配置不需要进行认证拦截的URL路径。这些路径将跳过Sa-Token的认证检查，
         * 通常用于静态资源、公开API等不需要认证的接口。
         * </p>
         *
         * <h3>路径匹配规则：</h3>
         * <ul>
         *   <li>支持Ant风格的路径匹配</li>
         *   <li>支持通配符 * 和 **</li>
         *   <li>支持文件扩展名匹配</li>
         * </ul>
         */
        private List<String> ignored;

        /**
         * 获取所有忽略路径列表
         * <p>
         * 合并默认忽略路径和用户自定义忽略路径，返回完整的忽略路径列表。
         * 如果用户未配置忽略路径，则返回默认忽略路径列表。
         * </p>
         *
         * @return 完整的忽略路径列表
         */
        public List<String> getAllIgnoreList() {
            if (CollUtil.isEmpty(ignored)) {
                return DEFAULT_IGNORE_PATH_LIST;
            }
            return CollUtil.addAllIfNotContains(DEFAULT_IGNORE_PATH_LIST, ignored);
        }

        /**
         * 登录类型配置列表
         * <p>
         * 支持多种登录类型的配置，每种登录类型可以有不同的参数设置。
         * 用于扩展Sa-Token的登录类型支持。
         * </p>
         */
        private List<KeyValue> loginTypes = Lists.newArrayList();
    }

    /**
     * HMAC认证配置
     * <p>
     * 配置基于HMAC-SHA算法的API认证机制。适用于服务间调用、
     * 第三方API接入等需要高安全性的场景。
     * </p>
     */
    @NestedConfigurationProperty
    Hmac hmac = new Hmac();

    /**
     * HMAC认证配置类
     * <p>
     * 定义HMAC认证的相关配置，包括是否启用HMAC认证以及允许的客户端列表。
     * 每个客户端都有唯一的AppId和Secret用于签名验证。
     * </p>
     *
     * <h3>安全机制：</h3>
     * <ul>
     *   <li>基于HMAC-SHA算法的消息认证</li>
     *   <li>防重放攻击保护</li>
     *   <li>客户端身份验证</li>
     *   <li>请求完整性校验</li>
     * </ul>
     */
    @Getter
    @Setter
    public static class Hmac {
        /**
         * 是否启用HMAC认证
         * <p>
         * 控制HMAC认证功能的总开关。启用后，系统将对配置的API接口
         * 进行HMAC签名验证。
         * </p>
         *
         * @default false
         */
        boolean enabled = false;

        /**
         * 允许的HMAC客户端列表
         * <p>
         * 配置允许访问的客户端信息，每个客户端包含唯一的AppId和Secret。
         * 客户端使用Secret对请求进行签名，服务端使用相同的Secret验证签名。
         * </p>
         */
        List<Client> clients = new ArrayList<>();

        /**
         * HMAC客户端配置类
         * <p>
         * 定义单个HMAC客户端的认证信息，包括客户端标识和密钥。
         * 每个客户端都有唯一的AppId和Secret组合用于身份验证。
         * </p>
         *
         * <h3>使用说明：</h3>
         * <ul>
         *   <li>AppId用于标识客户端身份</li>
         *   <li>Secret用于生成和验证HMAC签名</li>
         *   <li>Secret应保密存储，不可泄露</li>
         * </ul>
         */
        @Getter
        @Setter
        public static class Client {
            /**
             * 客户端应用标识
             * <p>
             * 唯一标识客户端应用的ID，用于区分不同的API调用方。
             * 在HMAC认证过程中作为客户端身份的标识符。
             * </p>
             */
            String appid;

            /**
             * 客户端密钥
             * <p>
             * 用于HMAC签名计算的密钥，必须与客户端保持一致。
             * 该密钥用于生成请求签名和验证请求的完整性。
             * </p>
             *
             * <strong>安全提示：</strong>密钥应妥善保管，避免泄露。
             */
            String secret;
        }
    }

    /**
     * 表单登录配置
     * <p>
     * 配置基于用户名密码的传统表单登录方式。支持自定义登录页面、
     * 登录处理URL、验证码等功能。
     * </p>
     */
    @NestedConfigurationProperty
    Form form = new Form();

    /**
     * 表单登录配置类
     * <p>
     * 定义表单登录的相关配置，包括登录页面、处理URL、验证码设置等。
     * 提供灵活的表单登录定制能力。
     * </p>
     *
     * <h3>核心功能：</h3>
     * <ul>
     *   <li>自定义登录页面和处理URL</li>
     *   <li>验证码集成支持</li>
     *   <li>登录参数配置</li>
     *   <li>安全防护机制</li>
     * </ul>
     */
    @Getter
    @Setter
    public static class Form {
        /**
         * 是否启用表单登录
         * <p>
         * 控制表单登录功能的总开关。启用后，系统将提供基于用户名密码的
         * 传统登录方式。
         * </p>
         *
         * @default false
         */
        boolean enabled = false;

        /**
         * 登录页面路径
         * <p>
         * 指定用户登录页面的URL路径。当用户未认证时，系统将重定向到此页面。
         * 支持相对路径和绝对路径。
         * </p>
         *
         * @default "/login.html"
         */
        String loginPage = "/login.html";

        /**
         * 登录处理URL
         * <p>
         * 指定处理登录请求的URL路径。前端登录表单应该提交到此URL。
         * 系统将在此URL上处理用户名密码验证。
         * </p>
         *
         * @default "/login"
         */
        String loginProcessingUrl = "/login";

        /**
         * 是否启用验证码
         * <p>
         * 控制登录时是否需要验证码验证。启用后可以防止暴力破解攻击，
         * 提高登录安全性。
         * </p>
         *
         * @default false
         */
        boolean enableVerify = false;

        /**
         * 表单请求参数配置
         * <p>
         * 定义表单登录时的请求参数名称，如用户名字段、密码字段、验证码字段等。
         * 允许自定义参数名称以适应不同的前端实现。
         * </p>
         */
        @NestedConfigurationProperty
        Parameters parameters = new Parameters();

        /**
         * 验证码动态触发配置
         * <p>
         * 配置基于登录失败次数的验证码动态触发策略。
         * </p>
         */
        @NestedConfigurationProperty
        CaptchaTrigger captchaTrigger = new CaptchaTrigger();

        /**
         * 锁定策略配置
         * <p>
         * 配置登录失败时的账户锁定策略，用于防止暴力破解攻击。
         * 支持基于Redis的分布式锁定机制。
         * </p>
         */
        @NestedConfigurationProperty
        LockStrategy lockStrategy = new LockStrategy();

        /**
         * 限流策略配置
         * <p>
         * 配置登录请求的限流策略，防止频繁登录尝试。
         * 目前主要针对短信登录方式有效，可以限制短信发送频率。
         * </p>
         */
        @NestedConfigurationProperty
        RateLimiterStrategy rateStrategy = new RateLimiterStrategy();

        /**
         * 登出配置
         * <p>
         * 定义用户登出相关的配置，包括登出URL、登出成功页面等。
         * </p>
         */
        @NestedConfigurationProperty
        Logout logout = new Logout();

        /**
         * 登出配置类
         * <p>
         * 定义用户登出相关的URL配置，包括登出处理地址和登出成功后的跳转地址。
         * </p>
         */
        @Getter
        @Setter
        public static class Logout {
            /**
             * 登出处理URL
             * <p>
             * 指定处理用户登出请求的URL路径。用户访问此URL时将执行登出操作，
             * 清除会话信息并使用户退出登录状态。
             * </p>
             *
             * @default "/logout"
             */
            private String logoutUrl = "/logout";

            /**
             * 登出成功跳转URL
             * <p>
             * 用户成功登出后重定向到的页面URL。通常重定向到登录页面，
             * 并可以携带参数提示用户已成功登出。
             * </p>
             *
             * @default "/login?logout"
             */
            private String logoutSuccessUrl = "/login?logout";
        }

        /**
         * 表单登录参数配置类
         * <p>
         * 定义表单登录时HTTP请求中的参数名称。允许自定义用户名和密码字段的名称，
         * 以适应不同的前端表单实现。
         * </p>
         *
         * <h3>使用场景：</h3>
         * <ul>
         *   <li>适配现有前端表单字段名称</li>
         *   <li>提高安全性，避免使用默认字段名</li>
         *   <li>支持多语言环境下的字段命名</li>
         * </ul>
         */
        @Getter
        @Setter
        public static class Parameters {
            /**
             * 用户名参数名称
             * <p>
             * 指定表单中用户名输入框的name属性值。登录时系统将从HTTP请求中
             * 根据此参数名获取用户名。
             * </p>
             *
             * @default "username"
             */
            private String username = "username";

            /**
             * 密码参数名称
             * <p>
             * 指定表单中密码输入框的name属性值。登录时系统将从HTTP请求中
             * 根据此参数名获取密码。
             * </p>
             *
             * @default "password"
             */
            private String password = "password";
        }

        /**
         * 验证码动态触发配置类
         * <p>
         * 定义基于登录失败次数的验证码动态触发策略配置。
         * </p>
         */
        @Getter
        @Setter
        public static class CaptchaTrigger {
            /**
             * 是否启用动态验证码触发
             * <p>
             * 启用后，当用户登录失败次数达到 failureTriggerTimes 时，
             * 会动态要求用户输入验证码，而不是全局强制验证码。
             * </p>
             *
             * @default false
             */
            private boolean enabled = false;

            /**
             * 触发验证码的失败次数阈值
             * <p>
             * 当用户连续登录失败达到此次数时，开始要求输入验证码。
             * 此值必须小于 lockStrategy.failureMaxTimes。
             * </p>
             *
             * @default 2
             */
            private int failureTriggerTimes = 2;
        }

        /**
         * 锁定策略配置类
         * <p>
         * 定义账户锁定相关的策略配置，用于防止暴力破解攻击。
         * 当用户登录失败次数达到阈值时，将锁定账户一段时间。
         * </p>
         *
         * <h3>安全机制：</h3>
         * <ul>
         *   <li>基于失败次数的自动锁定</li>
         *   <li>可配置的锁定时间</li>
         *   <li>支持Redis分布式锁定</li>
         *   <li>防止暴力破解攻击</li>
         * </ul>
         */
        @Getter
        @Setter
        public static class LockStrategy {
            /**
             * 登录失败最大次数
             * <p>
             * 设置用户连续登录失败的最大允许次数。当失败次数达到此阈值时，
             * 账户将被锁定。设置为0表示不限制失败次数。
             * </p>
             *
             * @default 3
             */
            private int failureMaxTimes = 3;

            /**
             * 锁定持续时间
             * <p>
             * 当登录失败次数达到最大值后，账户被锁定的时间长度。
             * 锁定期间用户无法进行登录操作。
             * </p>
             *
             * @default 1
             */
            private int duration = 1;

            /**
             * 锁定时间单位
             * <p>
             * 指定锁定持续时间的时间单位。可以是秒、分钟、小时等。
             * 与duration字段配合使用确定具体的锁定时长。
             * </p>
             *
             * @default TimeUnit.HOURS
             */
            private TimeUnit timeUnit = TimeUnit.HOURS;
        }

        /**
         * 限流策略配置类
         * <p>
         * 定义请求限流相关的策略配置，用于防止频繁请求攻击。
         * 在指定时间窗口内限制请求次数，超过限制将被拒绝。
         * </p>
         *
         * <h3>限流机制：</h3>
         * <ul>
         *   <li>基于时间窗口的请求计数</li>
         *   <li>可配置的请求频率限制</li>
         *   <li>防止恶意频繁请求</li>
         *   <li>保护系统资源</li>
         * </ul>
         */
        @Getter
        @Setter
        public static class RateLimiterStrategy {
            /**
             * 最大请求次数
             * <p>
             * 在指定时间窗口内允许的最大请求次数。超过此次数的请求将被拒绝，
             * 主要用于防止频繁的登录尝试或短信发送请求。
             * </p>
             *
             * @default 10
             */
            private int maxTimes = 10;

            /**
             * 时间窗口长度
             * <p>
             * 限流统计的时间窗口长度。在此时间段内统计请求次数，
             * 与timeUnit配合确定具体的时间窗口大小。
             * </p>
             *
             * @default 1
             */
            private int duration = 1;

            /**
             * 时间窗口单位
             * <p>
             * 指定时间窗口长度的时间单位。可以是秒、分钟、小时等。
             * 与duration字段配合使用确定具体的时间窗口。
             * </p>
             *
             * @default TimeUnit.HOURS
             */
            private TimeUnit timeUnit = TimeUnit.HOURS;
        }
    }

    /**
     * 验证码配置
     * <p>
     * 配置图形验证码相关的参数，包括验证码样式、有效期、获取地址等。
     * 用于防止自动化攻击和提高登录安全性。
     * </p>
     */
    @NestedConfigurationProperty
    Verify verify = new Verify();

    /**
     * 验证码配置类
     * <p>
     * 定义图形验证码的详细配置参数，包括验证码的外观、有效期、获取方式等。
     * 支持开发模式和生产模式的不同配置。
     * </p>
     *
     * <h3>主要功能：</h3>
     * <ul>
     *   <li>自定义验证码样式和尺寸</li>
     *   <li>配置验证码有效期</li>
     *   <li>支持开发模式调试</li>
     *   <li>防止自动化攻击</li>
     * </ul>
     */
    @Data
    public static class Verify {
        /**
         * 开发模式开关
         * <p>
         * 在开发模式下，验证码验证可能会被简化或跳过，便于开发调试。
         * 生产环境应设置为false以确保安全性。
         * </p>
         *
         * @default false
         */
        private boolean devMode = false;

        /**
         * 验证码获取URL
         * <p>
         * 前端获取验证码图片的API地址。用户访问此URL可以获取新的验证码图片。
         * </p>
         *
         * @default "/jcaptcha"
         */
        private String url = "/jcaptcha";

        /**
         * 验证码有效期
         * <p>
         * 验证码的有效时间长度，超过此时间验证码将失效。
         * 与timeUnit配合确定具体的有效期。
         * </p>
         *
         * @default 180
         */
        private int duration = 180;

        /**
         * 有效期时间单位
         * <p>
         * 指定验证码有效期的时间单位。与duration字段配合使用。
         * </p>
         *
         * @default TimeUnit.SECONDS
         */
        private TimeUnit timeUnit = TimeUnit.SECONDS;

        /**
         * 验证码类型
         * <p>
         * 指定验证码的类型，如数字、字母、混合等。
         * 不同类型的验证码有不同的安全级别。
         * </p>
         */
        private String captchaType;

        /**
         * 验证码图片宽度
         * <p>
         * 生成的验证码图片的像素宽度。
         * </p>
         *
         * @default 180
         */
        private int captchaWidth = 180;

        /**
         * 验证码图片高度
         * <p>
         * 生成的验证码图片的像素高度。
         * </p>
         *
         * @default 70
         */
        private int captchaHeight = 70;

        /**
         * 验证码字符数量
         * <p>
         * 验证码中包含的字符个数。字符数量越多，安全性越高，
         * 但用户输入难度也会增加。
         * </p>
         *
         * @default 4
         */
        private int captchaCodeCount = 4;

        /**
         * 数字长度
         * <p>
         * 当验证码类型为数字时，每个数字的长度配置。
         * </p>
         *
         * @default 1
         */
        private int captchaNumberLength = 1;
    }

    /**
     * XSS防护配置
     * <p>
     * 配置跨站脚本攻击(XSS)的防护机制。通过过滤和转义用户输入，
     * 防止恶意脚本注入攻击。
     * </p>
     */
    @NestedConfigurationProperty
    XssProtected xssProtected = new XssProtected();

    /**
     * XSS防护配置类
     * <p>
     * 定义XSS攻击防护的相关配置，包括是否启用防护以及信任的域名列表。
     * 通过白名单机制允许特定来源的内容。
     * </p>
     *
     * <h3>防护机制：</h3>
     * <ul>
     *   <li>输入内容过滤和转义</li>
     *   <li>恶意脚本检测和阻止</li>
     *   <li>白名单信任机制</li>
     *   <li>HTTP头安全设置</li>
     * </ul>
     */
    @Getter
    @Setter
    public static class XssProtected {
        /**
         * 是否启用XSS防护
         * <p>
         * 控制XSS防护功能的总开关。启用后，系统将对用户输入进行
         * XSS攻击检测和防护处理。
         * </p>
         *
         * @default false
         */
        boolean enabled = false;

        /**
         * 信任的域名集合
         * <p>
         * 配置不需要进行XSS防护的可信域名列表。来自这些域名的请求
         * 将跳过XSS检测，通常用于内部系统或可信的第三方服务。
         * </p>
         */
        Set<String> trusted = Sets.newHashSet();
    }

    /**
     * 短信登录配置
     * <p>
     * 配置基于短信验证码的登录方式。用户通过手机号接收验证码进行身份验证，
     * 适用于移动端应用和需要快速登录的场景。
     * </p>
     */
    @NestedConfigurationProperty
    SmsLogin sms = new SmsLogin();

    /**
     * 短信登录配置类
     * <p>
     * 定义短信验证码登录的详细配置，包括API地址、参数名称、有效期等。
     * 提供灵活的短信登录定制能力。
     * </p>
     *
     * <h3>核心功能：</h3>
     * <ul>
     *   <li>短信验证码发送和验证</li>
     *   <li>防刷机制和频率控制</li>
     *   <li>可配置的有效期和重发间隔</li>
     *   <li>测试模式支持</li>
     * </ul>
     */
    @Setter
    @Getter
    public static class SmsLogin {
        /**
         * 是否启用短信登录
         * <p>
         * 控制短信登录功能的总开关。启用后，用户可以通过手机号和短信验证码
         * 进行登录认证。
         * </p>
         *
         * @default false
         */
        boolean enabled = false;

        /**
         * 短信登录处理URL
         * <p>
         * 处理短信登录请求的API地址。前端应将手机号和验证码提交到此URL
         * 进行登录验证。
         * </p>
         *
         * @default "/sms-login"
         */
        String loginPath = "/sms-login";

        /**
         * 短信验证码获取URL
         * <p>
         * 获取短信验证码的API地址。用户通过此URL请求发送短信验证码
         * 到指定手机号。
         * </p>
         *
         * @default "/sms-code"
         */
        String verifyPath = "/sms-code";

        /**
         * 验证码参数名
         * <p>
         * 短信验证码在HTTP请求中的参数名称。登录时系统将根据此参数名
         * 获取用户输入的验证码。
         * </p>
         *
         * @default "code"
         */
        String code = "code";

        /**
         * 手机号参数名
         * <p>
         * 手机号在HTTP请求中的参数名称。系统将根据此参数名获取用户的
         * 手机号码。
         * </p>
         *
         * @default "mobile"
         */
        String mobile = "mobile";

        /**
         * 验证码有效期(分钟)
         * <p>
         * 短信验证码的有效时间，以分钟为单位。超过此时间的验证码将失效，
         * 用户需要重新获取。
         * </p>
         *
         * @default 3
         */
        int validMinutes = 3;

        /**
         * 验证码重发间隔(秒)
         * <p>
         * 两次短信验证码发送之间的最小间隔时间，以秒为单位。
         * 防止用户频繁请求短信验证码。
         * </p>
         *
         * @default 60
         */
        int resendSeconds = 60;

        /**
         * 是否启用图形验证码
         * <p>
         * 在获取短信验证码前是否需要先验证图形验证码。启用后可以防止
         * 恶意刷短信攻击。
         * </p>
         *
         * @default false
         */
        boolean enableVerify = false;

        /**
         * 测试模式开关
         * <p>
         * 启用测试模式后，系统不会真正发送短信，而是使用模拟的验证码。
         * 便于开发和测试环境使用，避免产生短信费用。
         * </p>
         *
         * @default false
         */
        boolean mock = false;
    }

    /**
     * 第三方登录配置
     * <p>
     * 配置各种第三方平台的登录集成，如微信小程序、微信公众号等。
     * 支持OAuth2.0授权流程和统一的第三方登录处理。
     * </p>
     */
    @NestedConfigurationProperty
    ThirdPartLogin thirdPartLogin = new ThirdPartLogin();

    /**
     * 第三方登录配置类
     * <p>
     * 定义第三方登录的统一配置，包括登录处理地址、参数名称等。
     * 提供统一的第三方登录接口和参数规范。
     * </p>
     *
     * <h3>支持的平台：</h3>
     * <ul>
     *   <li>微信小程序(WeChat Mini Program)</li>
     *   <li>微信公众号(WeChat Official Account)</li>
     *   <li>其他OAuth2.0兼容平台</li>
     * </ul>
     */
    @Setter
    @Getter
    public static class ThirdPartLogin {

        /**
         * 是否启用第三方登录
         * <p>
         * 控制第三方登录功能的总开关。启用后，用户可以通过配置的
         * 第三方平台进行登录认证。
         * </p>
         *
         * @default false
         */
        boolean enabled = false;

        /**
         * 第三方登录处理URL
         * <p>
         * 统一处理所有第三方登录请求的API地址。不同的第三方平台
         * 都通过此URL进行登录处理。
         * </p>
         *
         * @default "/thirdPart-login"
         */
        String loginPath = "/thirdPart-login";

        /**
         * 第三方类型参数名
         * <p>
         * 在HTTP请求中标识第三方平台类型的参数名称。用于区分
         * 不同的第三方登录平台。
         * </p>
         *
         * @default "thirdType"
         */
        String thirdName = "thirdType";

        /**
         * 第三方认证参数名
         * <p>
         * 第三方平台返回的认证参数在HTTP请求中的参数名称。
         * 通常包含授权码或访问令牌等认证信息。
         * </p>
         *
         * @default "thirdAuthParam"
         */
        String thirdAuthParam = "thirdAuthParam";

        /**
         * 构建授权URL参数名
         * <p>
         * 用于构建第三方平台授权URL的参数名称。前端可以通过此参数
         * 获取跳转到第三方授权页面的URL。
         * </p>
         *
         * @default "buildAuthorizationUrl"
         */
        String buildAuthorizationUrl = "buildAuthorizationUrl";

        /**
         * 微信小程序登录配置
         */
        WxMa wxMa = new WxMa();

        /**
         * 微信小程序登录配置类
         * <p>
         * 配置微信小程序登录所需的应用信息。包括小程序的AppId和Secret，
         * 用于与微信服务器进行认证交互。
         * </p>
         *
         * <h3>配置说明：</h3>
         * <ul>
         *   <li>AppId: 微信小程序的唯一标识</li>
         *   <li>Secret: 微信小程序的密钥</li>
         *   <li>用于code2Session接口调用</li>
         * </ul>
         */
        @Setter
        @Getter
        public static class WxMa {

            /**
             * 是否启用微信小程序登录
             * <p>
             * 控制微信小程序登录功能的开关。启用后，用户可以通过
             * 微信小程序的授权码进行登录。
             * </p>
             *
             * @default false
             */
            boolean enabled = false;

            /**
             * 微信小程序AppId
             * <p>
             * 微信小程序的应用标识，从微信公众平台获取。
             * 用于标识具体的小程序应用。
             * </p>
             */
            String appId;

            /**
             * 微信小程序Secret
             * <p>
             * 微信小程序的应用密钥，从微信公众平台获取。
             * 用于调用微信API时的身份验证。
             * </p>
             *
             * <strong>安全提示：</strong>Secret应妥善保管，避免泄露。
             */
            String secret;
        }

        /**
         * 微信公众号登录配置类
         * <p>
         * 配置微信公众号登录所需的应用信息。支持微信公众号的OAuth2.0授权登录，
         * 用户可以通过微信公众号进行身份认证。
         * </p>
         *
         * <h3>功能特性：</h3>
         * <ul>
         *   <li>支持微信公众号OAuth2.0授权</li>
         *   <li>获取用户基本信息</li>
         *   <li>静默授权和用户授权</li>
         *   <li>网页授权登录</li>
         * </ul>
         */
        @Setter
        @Getter
        public static class WxMp {

            /**
             * 是否启用微信公众号登录
             * <p>
             * 控制微信公众号登录功能的开关。启用后，用户可以通过
             * 微信公众号的网页授权进行登录。
             * </p>
             *
             * @default false
             */
            boolean enabled = false;
        }
    }
}
