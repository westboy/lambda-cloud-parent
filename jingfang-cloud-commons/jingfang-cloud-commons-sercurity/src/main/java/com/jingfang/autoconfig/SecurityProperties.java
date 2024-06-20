package com.jingfang.autoconfig;

import cn.dev33.satoken.util.SaTokenConsts;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * SecurityProperties
 *
 * @author jpjoo
 */
@Data
@ConfigurationProperties(prefix = "jf.security")
public class SecurityProperties {

    private static final List<String> DEFAULT_IGNORE_PATH_LIST = Lists.newArrayList(
            "/public/**",
            "/anon/**",
            "*.html",
            "*.css",
            "*.ico",
            "*.js");

    private String tokenName = "Authorization";
    private String tokenPrefix = "Bearer";
    private String tokenStyle = SaTokenConsts.TOKEN_STYLE_RANDOM_32;
    private Integer tokenTimeout = 30 * 24 * 60 * 60;
    /**
     * 设置是否打开注解鉴权：配置为 true 时注解鉴权才会生效，配置为 false 时，即使写了注解也不会进行鉴权
     */
    private Boolean enableMethodAnnotation =true;
    /**
     * 是否允许同一账号多地同时登录（为 true 时允许一起登录，为 false 时新登录挤掉旧登录）
     */
    private Boolean enableKickOut = true;
    /**
     * 在多人登录同一账号时，是否共用一个 token （为 true 时所有登录共用一个 token，为 false 时每次登录新建一个 token）
     */
    private Boolean enableTokenShare = true;

    private Boolean enableLogPrint = false;

    private String jwtSecretKey = "jf-token-secret";

    private List<String> ignored;

    private Long activeTimeout = -1L;

    public List<String> getAllIgnoreList() {
        return CollUtil.addAllIfNotContains(DEFAULT_IGNORE_PATH_LIST, ignored);
    }

    @NestedConfigurationProperty
    public Form form = new Form();

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
         * 表单请求参数
         */
        @NestedConfigurationProperty
        Parameters parameters = new Parameters();
        /**
         * 验证码相关配置
         */
        @NestedConfigurationProperty
        Verify verify = new Verify();
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

    @Data
    public static class Verify {
        /**
         * 是否开启验证码
         */
        private boolean enabled = false;

        /**
         * 验证码获取地址
         */
        private String url = "/jcaptcha";

        private int duration = 180;

        private TimeUnit timeUnit = TimeUnit.SECONDS;
    }

    @NestedConfigurationProperty
    XssProtected xssProtected = new XssProtected();

    @Getter
    @Setter
    public static class XssProtected {
        boolean enabled = false;
        Set<String> trusted = Sets.newHashSet();
    }
}
