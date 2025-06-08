package com.lambda.autoconfig;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * SecurityProperties
 *
 * @author jpjoo
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

    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "springboot properties")
    @Getter
    @Setter
    public static class ExtendSaTokenConfig extends SaTokenConfig {
        /**
         * 设置是否打开注解鉴权：配置为 true 时注解鉴权才会生效，配置为 false 时，即使写了注解也不会进行鉴权
         */
        private Boolean enableMethodAuthentication = true;

        /**
         * 忽略拦截的配置
         */
        private List<String> ignored;

        public List<String> getAllIgnoreList() {
            if (CollUtil.isEmpty(ignored)) {
                return DEFAULT_IGNORE_PATH_LIST;
            }
            return CollUtil.addAllIfNotContains(DEFAULT_IGNORE_PATH_LIST, ignored);
        }
    }

    /**
     * HMac配置
     */
    @NestedConfigurationProperty
    Hmac hmac = new Hmac();

    @Getter
    @Setter
    public static class Hmac {
        /**
         * 是否开启HMac认证
         */
        boolean enabled = false;
        /**
         * 允许的客户端
         */
        List<Client> clients = new ArrayList<>();

        @Getter
        @Setter
        public static class Client {
            /**
             * 客户端ID
             */
            String appid;
            /**
             * 客户端密钥
             */
            String secret;
        }
    }

    /**
     * 表单登陆配置
     */
    @NestedConfigurationProperty
    Form form = new Form();

    @Getter
    @Setter
    public static class Form {
        /**
         * 是否开启表单登陆
         */
        boolean enabled = false;
        /**
         * 登陆页面
         */
        String loginPage = "/login.html";

        /**
         * 登陆请求
         */
        String loginProcessingUrl = "/login";

        /**
         * 请用验证码 不开启验证码登录
         */
        boolean enableVerify = false;

        /**
         * 表单请求参数
         */
        @NestedConfigurationProperty
        Parameters parameters = new Parameters();

        /**
         * 缓存相关配置
         */
        @NestedConfigurationProperty
        LockStrategy lockStrategy = new LockStrategy();

        /**
         * 流量限制策略（目前只针对发送短信登陆方式有效）
         */
        @NestedConfigurationProperty
        RateLimiterStrategy rateStrategy = new RateLimiterStrategy();

        @NestedConfigurationProperty
        Logout logout = new Logout();

        @Getter
        @Setter
        public static class Logout {
            /**
             * 登出处理器地址
             */
            private String logoutUrl = "/logout";
            /**
             * 登出成功后跳转地址
             */
            private String logoutSuccessUrl = "/login?logout";
        }

        /**
         * 表单登录相关参数
         */
        @Getter
        @Setter
        public static class Parameters {
            /**
             * 用户名称的参数名称
             */
            private String username = "username";
            /**
             * 用户密码的参数名称
             */
            private String password = "password";
        }

        @Getter
        @Setter
        public static class LockStrategy {
            /**
             * 登陆失败最大次数,0不限次数
             */
            private int failureMaxTimes = 3;
            /**
             * 登陆失败次数达到后锁定时长
             */
            private int duration = 1;
            /**
             * 锁定时间单位
             */
            private TimeUnit timeUnit = TimeUnit.HOURS;
        }

        @Getter
        @Setter
        public static class RateLimiterStrategy {
            /**
             * 在持续时间内，允许请求的最大次数
             */
            private int maxTimes = 10;
            /**
             * 持续时间
             */
            private int duration = 1;
            /**
             * 持续时间单位
             */
            private TimeUnit timeUnit = TimeUnit.HOURS;
        }
    }

    /**
     * 验证码相关配置
     */
    @NestedConfigurationProperty
    Verify verify = new Verify();

    @Data
    public static class Verify {
        /**
         * 开发模式
         */
        private boolean devMode = false;

        /**
         * 验证码获取地址
         */
        private String url = "/jcaptcha";

        private int duration = 180;

        private TimeUnit timeUnit = TimeUnit.SECONDS;

        private String captchaType;

        private int captchaWidth = 180;

        private int captchaHeight = 70;

        private int captchaCodeCount = 4;

        private int captchaNumberLength = 1;
    }

    /**
     * Xss防护配置
     */
    @NestedConfigurationProperty
    XssProtected xssProtected = new XssProtected();

    @Getter
    @Setter
    public static class XssProtected {
        boolean enabled = false;
        Set<String> trusted = Sets.newHashSet();
    }

    /**
     * 短信登陆配置
     */
    @NestedConfigurationProperty
    SmsLogin sms = new SmsLogin();

    @Setter
    @Getter
    public static class SmsLogin {
        /**
         * 是否开启短信登录
         */
        boolean enabled = false;

        /**
         * 短信登录地址
         */
        String loginPath = "/sms-login";
        /**
         * 短信验证码地址
         */
        String verifyPath = "/sms-code";
        /**
         * 验证码接收的参数名
         */
        String code = "code";
        /**
         * 手机号接收参数名
         */
        String mobile = "mobile";

        /**
         * 短信验证码有效分钟
         */
        int validMinutes = 3;

        /**
         * 短信验证码可重发秒
         */
        int resendSeconds = 60;

        /**
         * 请用验证码 不开启验证码登录
         */
        boolean enableVerify = false;
    }

    /**
     * 第三方登录
     */
    @NestedConfigurationProperty
    ThirdPartLogin thirdPartLogin = new ThirdPartLogin();

    @Setter
    @Getter
    public static class ThirdPartLogin {

        boolean enabled = false;

        String loginPath = "/thirdPart-login";

        String thirdName = "thirdType";

        String thirdAuthCode = "thirdAuthParam";

        String buildAuthorizationUrl = "buildAuthorizationUrl";


        WxMa wxMa = new WxMa();

        @Setter
        @Getter
        public static class WxMa {

            boolean enabled = false;

            String appId;

            String secret;

        }

        @Setter
        @Getter
        public static class WxMp {

            boolean enabled = false;

        }

    }
}
