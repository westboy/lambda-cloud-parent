package com.lambda.autoconfig;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaRedissonConfigImpl;
import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpLogic;
import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.cloud.core.exception.model.ErrorModel;
import com.lambda.cloud.core.shared.KeyValue;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.cloud.redis.helper.RedisHelper;
import com.lambda.cloud.sms.SmsMessageSender;
import com.lambda.security.encoder.HmacShaEncoder;
import com.lambda.security.encoder.StandardPasswordEncoder;
import com.lambda.security.handler.LogoutHandler;
import com.lambda.security.handler.LogoutSuccessHandler;
import com.lambda.security.handler.impl.CommonAuthenticationFailureHandler;
import com.lambda.security.handler.impl.CommonAuthenticationSuccessHandler;
import com.lambda.security.handler.impl.CommonLogoutHandler;
import com.lambda.security.handler.impl.CommonLogoutSuccessHandler;
import com.lambda.security.inteceptor.SaTokenInterceptor;
import com.lambda.security.inteceptor.SecureInterceptor;
import com.lambda.security.provider.ThirdPartLoginProvider;
import com.lambda.security.provider.wx.WxMaLoginHandler;
import com.lambda.security.provider.wx.WxMaLoginProvider;
import com.lambda.security.service.HmacClientService;
import com.lambda.security.service.ThirdPartyLoginService;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.form.FormAuthenticationProcessingFilter;
import com.lambda.security.web.form.FormLockingStrategy;
import com.lambda.security.web.form.FormLoginValidator;
import com.lambda.security.web.form.FormLogoutFilter;
import com.lambda.security.web.form.locking.RedisLockingStrategy;
import com.lambda.security.web.hmac.HmacAuthenticationProcessingFilter;
import com.lambda.security.web.hmac.handler.HmacAuthenticationSuccessHandler;
import com.lambda.security.web.hmac.service.MemoryHmacClientService;
import com.lambda.security.web.sms.SmsAuthenticationProcessingFilter;
import com.lambda.security.web.third.ThirdPartAuthenticationProcessingFilter;
import com.lambda.security.web.verify.VerifyCodeFilter;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.service.captcha.CaptchaVerifyCodeGenerateImpl;
import com.lambda.security.web.verify.service.captcha.CaptchaVerifyCodeValidationImpl;
import com.lambda.security.web.verify.service.captcha.store.CaptchaStore;
import com.lambda.security.web.verify.service.captcha.store.RedisCaptchaStore;
import com.lambda.security.web.verify.service.sms.SmsVerifyCodeGenerateImpl;
import com.lambda.security.web.verify.service.sms.SmsVerifyCodeValidationImpl;
import com.lambda.security.web.verify.service.sms.store.RedisSmsVerifyCodeStore;
import com.lambda.security.web.verify.service.sms.store.SmsVerifyCodeStore;
import com.lambda.security.web.xss.XSSDefendFilter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Lambda Cloud 安全模块自动配置类
 * <p>
 * 该配置类提供了完整的安全认证和授权功能的自动配置，基于Sa-Token框架构建，
 * 支持多种认证方式和安全防护机制。
 * </p>
 *
 * <h3>主要功能模块：</h3>
 * <ul>
 *   <li><strong>Sa-Token配置</strong> - 核心认证框架配置，支持多登录类型</li>
 *   <li><strong>表单认证</strong> - 传统用户名密码登录方式</li>
 *   <li><strong>短信认证</strong> - 基于手机号和验证码的登录方式</li>
 *   <li><strong>HMAC认证</strong> - API签名认证，适用于服务间调用</li>
 *   <li><strong>第三方登录</strong> - 支持微信小程序等第三方平台登录</li>
 *   <li><strong>验证码服务</strong> - 图形验证码和短信验证码</li>
 *   <li><strong>XSS防护</strong> - 跨站脚本攻击防护</li>
 * </ul>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li>多重认证机制并存</li>
 *   <li>登录失败锁定策略</li>
 *   <li>请求频率限制</li>
 *   <li>同源令牌检查</li>
 *   <li>XSS攻击防护</li>
 *   <li>密码加密存储</li>
 * </ul>
 *
 * <h3>配置示例：</h3>
 * <pre>{@code
 * lambda:
 *   security:
 *     sa-token:
 *       token-name: "Authorization"
 *       timeout: 2592000
 *     form:
 *       enabled: true
 *       login-processing-url: "/login"
 *     sms:
 *       enabled: true
 *       valid-minutes: 5
 *     hmac:
 *       enabled: true
 *       clients:
 *         - appid: "client1"
 *           secret: "secret1"
 * }</pre>
 *
 * @author jpjoo
 * @see SecurityProperties
 * @see cn.dev33.satoken.SaManager
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@EnableConfigurationProperties({SecurityProperties.class})
public class SecurityAutoConfiguration {

    public SecurityAutoConfiguration() {
        log.trace("initializing...");
    }

    /**
     * XSS防护过滤器
     * <p>
     * 当启用XSS防护时，创建XSS防护过滤器来防止跨站脚本攻击。
     * 该过滤器会对请求参数进行清理，移除或转义潜在的恶意脚本代码。
     * </p>
     *
     * <h3>防护机制：</h3>
     * <ul>
     *   <li>过滤HTML标签和JavaScript代码</li>
     *   <li>转义特殊字符</li>
     *   <li>支持信任域名白名单</li>
     * </ul>
     *
     * @param securityProperties 安全配置属性
     * @return XSS防护过滤器实例
     */
    @Bean
    @ConditionalOnProperty(prefix = "lambda.security.xss-protected", name = "enabled")
    public XSSDefendFilter xssDefendFilter(SecurityProperties securityProperties) {
        SecurityProperties.XssProtected xssProtected = securityProperties.getXssProtected();
        return new XSSDefendFilter(xssProtected.trusted);
    }

    /**
     * Sa-Token核心配置类
     * <p>
     * 负责Sa-Token框架的核心配置，包括多登录类型支持、拦截器配置、
     * 同源令牌检查等功能。该配置类是整个安全模块的核心。
     * </p>
     *
     * <h3>主要功能：</h3>
     * <ul>
     *   <li>初始化多种登录类型（HMAC、用户登录等）</li>
     *   <li>配置Sa-Token核心参数</li>
     *   <li>设置安全拦截器</li>
     *   <li>配置同源令牌检查过滤器</li>
     * </ul>
     */
    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "springboot properties")
    @Configuration
    public static class SaTokenConfiguration {

        private SecurityProperties securityProperties;

        @Autowired
        public void setSecurityProperties(SecurityProperties securityProperties) {
            this.securityProperties = securityProperties;
        }

        /**
         * 初始化Sa-Token登录类型
         * <p>
         * 根据配置文件中定义的登录类型，动态创建对应的StpLogic实例，
         * 并注册到Sa-Token管理器中。支持多种登录类型并存。
         * </p>
         *
         * @return 应用启动运行器
         */
        @Bean
        public ApplicationRunner initSaToken() {
            return args -> {
                List<KeyValue> loginTypes = securityProperties.getSaToken().getLoginTypes();
                if (CollUtil.isNotEmpty(loginTypes)) {
                    Set<String> initializeLoginTypes =
                            loginTypes.stream().map(KeyValue::getCode).collect(Collectors.toSet());
                    initializeLoginTypes.forEach(type -> {
                        StpLogic newStpLogic = new StpLogic(type);
                        SaManager.putStpLogic(newStpLogic);
                    });
                    StpLogicUtils.initializeLoginTypes(initializeLoginTypes);
                    log.trace("init sa-token login types: {}", initializeLoginTypes);
                }
            };
        }

        /**
         * Sa-Token核心配置
         * <p>
         * 创建Sa-Token的核心配置对象，从配置文件中读取相关参数。
         * 该配置对象控制令牌的生成、验证、过期等核心行为。
         * </p>
         *
         * @return Sa-Token配置对象
         */
        @Bean
        @Primary
        @ConfigurationProperties(prefix = "lambda.security.sa-token")
        public SaTokenConfig getSaTokenConfig() {
            return new SaTokenConfig();
        }

        /**
         * Sa-Token安全拦截器
         * <p>
         * 创建Sa-Token的安全拦截器，用于在请求处理前进行权限验证。
         * 支持基于注解的方法级权限控制。
         * </p>
         *
         * @param secureInterceptor 自定义安全拦截器
         * @return Sa-Token拦截器实例
         */
        @Bean
        @ConditionalOnBean(SecureInterceptor.class)
        public SaInterceptor saInterceptor(SecureInterceptor secureInterceptor) {
            return new SaInterceptor(new SaTokenInterceptor(secureInterceptor))
                    .isAnnotation(securityProperties.getSaToken().getEnableMethodAuthentication());
        }

        /**
         * Web MVC拦截器配置
         * <p>
         * 配置Sa-Token拦截器到Spring MVC拦截器链中，
         * 设置拦截路径和排除路径。
         * </p>
         *
         * @param saInterceptor Sa-Token拦截器
         * @return Web MVC配置器
         */
        @Bean
        @ConditionalOnBean(SaInterceptor.class)
        @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
        public WebMvcConfigurer saTokenWebMvcConfigurer(SaInterceptor saInterceptor) {
            return new WebMvcConfigurer() {
                @SuppressWarnings("all")
                @Override
                public void addInterceptors(InterceptorRegistry interceptorRegistry) {
                    interceptorRegistry
                            .addInterceptor(saInterceptor)
                            .addPathPatterns("/**")
                            .excludePathPatterns(securityProperties.getSaToken().getAllIgnoreList());
                }
            };
        }

        /**
         * 同源令牌检查过滤器
         * <p>
         * 当启用同源令牌检查时，创建Sa-Token的Servlet过滤器来验证请求的合法性。
         * 该过滤器会检查请求是否携带有效的同源令牌，防止CSRF攻击。
         * </p>
         *
         * <h3>安全机制：</h3>
         * <ul>
         *   <li>验证请求的同源令牌</li>
         *   <li>排除HMAC认证请求</li>
         *   <li>支持路径白名单配置</li>
         *   <li>统一的错误响应格式</li>
         * </ul>
         *
         * @return Sa-Token Servlet过滤器
         */
        @Bean
        @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
        public SaServletFilter getSaServletFilter() {
            return new SaServletFilter()
                    .addInclude("/**")
                    .addExclude(
                            securityProperties.getSaToken().getAllIgnoreList().toArray(new String[0]))
                    .setAuth(e -> {
                        HttpServletRequest currentRequest = WebHttpUtils.getCurrentRequest();
                        boolean hmacRequest = WebHttpUtils.isHmacRequest(currentRequest);
                        if (!hmacRequest && securityProperties.getSaToken().getCheckSameToken()) {
                            SaSameUtil.checkCurrentRequestToken();
                        }
                    })
                    .setError(exception -> {
                        ErrorModel errorModel = new ErrorModel();
                        errorModel.setStatus(HttpStatus.UNAUTHORIZED.value());
                        if (exception instanceof SaTokenException saTokenException) {
                            errorModel.setError(String.valueOf(saTokenException.getCode()));
                        } else {
                            errorModel.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
                        }
                        errorModel.setTimestamp(System.currentTimeMillis());
                        errorModel.setMessage(exception.getMessage());
                        return errorModel.toJsonString();
                    });
        }
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public SaReactorFilter getSaReactorFilter(SecurityProperties securityProperties) {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude(securityProperties.getSaToken().getAllIgnoreList().toArray(new String[0]))
                .setAuth(run -> StpLogicUtils.getActiveStpLogic().checkLogin())
                .setError(e -> {
                    ErrorModel errorModel = new ErrorModel();
                    errorModel.setStatus(HttpStatus.UNAUTHORIZED.value());
                    errorModel.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
                    if (e instanceof SaTokenException saTokenException) {
                        errorModel.setError(String.valueOf(saTokenException.getCode()));
                    }
                    errorModel.setTimestamp(System.currentTimeMillis());
                    errorModel.setMessage(e.getMessage());
                    return errorModel.toJsonString();
                });
    }

    /**
     * 验证码配置类
     * <p>
     * 当启用表单登录或短信登录时，提供验证码相关的配置和服务。
     * 支持图形验证码和短信验证码两种类型。
     * </p>
     *
     * <h3>验证码类型：</h3>
     * <ul>
     *   <li><strong>图形验证码</strong> - 数学运算验证码，防止机器人攻击</li>
     *   <li><strong>短信验证码</strong> - 手机号验证码，用于身份验证</li>
     * </ul>
     *
     * <h3>存储方式：</h3>
     * <ul>
     *   <li>基于Redis的分布式存储</li>
     *   <li>支持过期时间配置</li>
     *   <li>防重复提交机制</li>
     * </ul>
     */
    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "springboot properties")
    @Configuration
    @ConditionalOnExpression("${lambda.security.form.enabled:false} || ${lambda.security.sms.enabled:false}")
    public static class VerifyConfiguration {
        private SecurityProperties securityProperties;

        @Autowired
        public void setSecurityProperties(SecurityProperties securityProperties) {
            this.securityProperties = securityProperties;
        }

        /**
         * Redis验证码存储器
         * <p>
         * 创建基于Redis的验证码存储实现，用于存储和验证图形验证码。
         * 支持分布式部署环境下的验证码共享。
         * </p>
         *
         * @param redisHelper Redis操作助手
         * @return 验证码存储器实例
         */
        @Bean
        public CaptchaStore redisCaptchaStore(RedisHelper redisHelper) {
            RedisCaptchaStore redisCaptchaStore = new RedisCaptchaStore();
            redisCaptchaStore.setRedisHelper(redisHelper);
            return redisCaptchaStore;
        }

        /**
         * 图形验证码生成服务
         * <p>
         * 创建图形验证码的生成服务，负责生成数学运算验证码图片。
         * 验证码内容会存储到Redis中，用于后续验证。
         * </p>
         *
         * @param objectMapper      JSON序列化工具
         * @param redisCaptchaStore 验证码存储器
         * @return 验证码生成服务
         */
        @Bean
        public VerifyCodeService captchaVerifyCodeGenerate(ObjectMapper objectMapper, CaptchaStore redisCaptchaStore) {
            return new CaptchaVerifyCodeGenerateImpl(securityProperties, objectMapper, redisCaptchaStore);
        }

        /**
         * 图形验证码验证服务
         * <p>
         * 创建图形验证码的验证服务，负责验证用户输入的验证码是否正确。
         * 验证通过后会从Redis中删除对应的验证码。
         * </p>
         *
         * @param redisCaptchaStore 验证码存储器
         * @return 验证码验证服务
         */
        @Bean
        public VerifyCodeService captchaVerifyCodeValidation(CaptchaStore redisCaptchaStore) {
            return new CaptchaVerifyCodeValidationImpl(securityProperties, redisCaptchaStore);
        }

        /**
         * 验证码过滤器注册Bean
         * <p>
         * 注册验证码过滤器到Servlet容器中，用于处理验证码的生成和验证请求。
         * 该过滤器会拦截指定URL的请求，根据请求类型执行验证码生成或验证操作。
         * </p>
         *
         * <h3>功能特性：</h3>
         * <ul>
         *   <li>支持验证码图片生成</li>
         *   <li>支持验证码验证</li>
         *   <li>高优先级执行</li>
         *   <li>可配置拦截路径</li>
         * </ul>
         *
         * @param verifyCodeServices 验证码服务列表
         * @param objectMapper       JSON序列化工具
         * @return 过滤器注册Bean
         */
        @Bean
        public FilterRegistrationBean<VerifyCodeFilter> verifyCodeFilter(
                List<VerifyCodeService> verifyCodeServices, ObjectMapper objectMapper) {
            FilterRegistrationBean<VerifyCodeFilter> filterRegistrationBean = new FilterRegistrationBean<>();
            VerifyCodeFilter verifyCodeFilter = new VerifyCodeFilter(verifyCodeServices);
            verifyCodeFilter.setAuthenticationFailureHandler(new CommonAuthenticationFailureHandler(objectMapper));
            filterRegistrationBean.setFilter(verifyCodeFilter);
            filterRegistrationBean.addUrlPatterns("/*");
            filterRegistrationBean.setOrder(20);
            return filterRegistrationBean;
        }
    }

    /**
     * 短信登录配置类
     * <p>
     * 当启用短信登录功能时，提供短信验证码登录相关的配置和服务。
     * 支持手机号+验证码的登录方式，适用于移动端应用。
     * </p>
     *
     * <h3>核心功能：</h3>
     * <ul>
     *   <li><strong>短信验证码发送</strong> - 向用户手机发送验证码</li>
     *   <li><strong>验证码验证</strong> - 验证用户输入的短信验证码</li>
     *   <li><strong>用户认证</strong> - 基于手机号进行用户身份认证</li>
     *   <li><strong>防刷机制</strong> - 支持图形验证码防刷</li>
     * </ul>
     *
     * <h3>安全特性：</h3>
     * <ul>
     *   <li>验证码有效期控制</li>
     *   <li>发送频率限制</li>
     *   <li>Redis分布式存储</li>
     *   <li>统一异常处理</li>
     * </ul>
     */
    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "springboot properties")
    @Configuration
    @ConditionalOnProperty(prefix = "lambda.security.sms", name = "enabled")
    public static class SmsConfiguration {

        private SecurityProperties securityProperties;

        @Autowired
        public void setSecurityProperties(SecurityProperties securityProperties) {
            this.securityProperties = securityProperties;
        }

        /**
         * 短信认证处理过滤器
         * <p>
         * 创建短信登录的认证处理过滤器，负责处理手机号+验证码的登录请求。
         * 该过滤器会验证短信验证码的有效性，并通过用户详情服务进行身份认证。
         * </p>
         *
         * <h3>处理流程：</h3>
         * <ol>
         *   <li>接收手机号和验证码</li>
         *   <li>验证短信验证码</li>
         *   <li>通过用户详情服务获取用户信息</li>
         *   <li>执行登录成功或失败处理</li>
         * </ol>
         *
         * @param objectMapper      JSON序列化工具
         * @param userDetailService 用户详情服务
         * @return 短信认证过滤器注册Bean
         */
        @Bean
        public FilterRegistrationBean<SmsAuthenticationProcessingFilter> smsAuthenticationProcessingFilter(
                ObjectMapper objectMapper, @Autowired(required = false) UserDetailService userDetailService) {
            Assert.notNull(userDetailService, "userDetailService must not be null");
            FilterRegistrationBean<SmsAuthenticationProcessingFilter> filterRegistrationBean =
                    new FilterRegistrationBean<>();
            SmsAuthenticationProcessingFilter processingFilter = new SmsAuthenticationProcessingFilter(
                    securityProperties.getSms().getLoginPath());
            processingFilter.setAuthenticationSuccessHandler(new CommonAuthenticationSuccessHandler(objectMapper));
            processingFilter.setAuthenticationFailureHandler(new CommonAuthenticationFailureHandler(objectMapper));
            processingFilter.setUserDetailService(userDetailService);
            filterRegistrationBean.setFilter(processingFilter);
            filterRegistrationBean.addUrlPatterns("/*");
            filterRegistrationBean.setOrder(30);
            return filterRegistrationBean;
        }

        /**
         * 短信验证码存储器
         * <p>
         * 创建基于Redis的短信验证码存储实现，用于存储和管理短信验证码。
         * 支持验证码的过期时间控制和分布式环境下的数据共享。
         * </p>
         *
         * @param stringRedisTemplate Redis字符串模板
         * @return 短信验证码存储器
         */
        @Bean
        public SmsVerifyCodeStore<String> smsVerifyCodeStore(StringRedisTemplate stringRedisTemplate) {
            return new RedisSmsVerifyCodeStore(securityProperties.getSms(), stringRedisTemplate);
        }

        /**
         * 短信验证码生成服务
         * <p>
         * 创建短信验证码的生成服务，负责生成随机验证码并通过短信发送给用户。
         * 支持防刷机制，可配合图形验证码使用。
         * </p>
         *
         * <h3>功能特性：</h3>
         * <ul>
         *   <li>随机验证码生成</li>
         *   <li>短信发送集成</li>
         *   <li>用户存在性验证</li>
         *   <li>防刷保护机制</li>
         *   <li>发送频率控制</li>
         * </ul>
         *
         * @param objectMapper       JSON序列化工具
         * @param smsVerifyCodeStore 短信验证码存储器
         * @param smsMessageSender   短信发送服务
         * @param redisCaptchaStore  图形验证码存储器
         * @param userDetailService  用户详情服务
         * @return 短信验证码生成服务
         */
        @Bean
        public VerifyCodeService smsVerifyCodeGenerate(
                ObjectMapper objectMapper,
                SmsVerifyCodeStore<String> smsVerifyCodeStore,
                SmsMessageSender smsMessageSender,
                CaptchaStore redisCaptchaStore,
                @Autowired(required = false) UserDetailService userDetailService) {
            Assert.notNull(userDetailService, "userDetailService must not be null");
            SmsVerifyCodeGenerateImpl smsVerifyCodeGenerate =
                    new SmsVerifyCodeGenerateImpl(securityProperties, objectMapper, smsVerifyCodeStore);
            smsVerifyCodeGenerate.setUserDetailService(userDetailService);
            smsVerifyCodeGenerate.setSmsMessageSender(smsMessageSender);
            smsVerifyCodeGenerate.setCaptchaStore(redisCaptchaStore);
            return smsVerifyCodeGenerate;
        }

        /**
         * 短信验证码验证服务
         * <p>
         * 创建短信验证码的验证服务，负责验证用户输入的短信验证码是否正确。
         * 验证成功后会从存储器中删除对应的验证码，防止重复使用。
         * </p>
         *
         * <h3>验证机制：</h3>
         * <ul>
         *   <li>验证码正确性检查</li>
         *   <li>验证码有效期检查</li>
         *   <li>一次性使用保证</li>
         *   <li>手机号匹配验证</li>
         * </ul>
         *
         * @param smsVerifyCodeStore 短信验证码存储器
         * @return 短信验证码验证服务
         */
        @Bean
        public VerifyCodeService smsVerifyCodeValidation(SmsVerifyCodeStore<String> smsVerifyCodeStore) {
            return new SmsVerifyCodeValidationImpl(securityProperties, smsVerifyCodeStore);
        }
    }

    /**
     * HMAC认证配置类
     * <p>
     * 当启用HMAC认证功能时，提供基于HMAC-SHA算法的API认证机制。
     * 适用于服务间调用、API接口安全认证等场景。
     * </p>
     *
     * <h3>HMAC认证原理：</h3>
     * <ul>
     *   <li><strong>密钥管理</strong> - 每个客户端分配唯一的AppId和Secret</li>
     *   <li><strong>签名生成</strong> - 使用HMAC-SHA算法对请求参数进行签名</li>
     *   <li><strong>时间戳验证</strong> - 防止重放攻击</li>
     *   <li><strong>签名验证</strong> - 服务端验证请求签名的有效性</li>
     * </ul>
     *
     * <h3>安全特性：</h3>
     * <ul>
     *   <li>无状态认证</li>
     *   <li>防重放攻击</li>
     *   <li>密钥安全传输</li>
     *   <li>请求完整性保护</li>
     * </ul>
     */
    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "springboot properties")
    @Configuration
    @ConditionalOnProperty(prefix = "lambda.security.hmac", name = "enabled")
    public static class HmacConfiguration {

        private SecurityProperties securityProperties;

        @Autowired
        public void setSecurityProperties(SecurityProperties securityProperties) {
            this.securityProperties = securityProperties;
        }

        /**
         * HMAC客户端服务
         * <p>
         * 创建HMAC客户端管理服务，负责管理客户端的AppId和Secret信息。
         * 默认使用内存存储，可通过自定义Bean替换为其他存储方式。
         * </p>
         *
         * <h3>功能职责：</h3>
         * <ul>
         *   <li>客户端信息存储和查询</li>
         *   <li>AppId和Secret的验证</li>
         *   <li>用户详情服务集成</li>
         *   <li>客户端权限管理</li>
         * </ul>
         *
         * @param userDetailService 用户详情服务（可选）
         * @return HMAC客户端服务实例
         */
        @Bean
        @ConditionalOnMissingBean
        public HmacClientService hmacClientService(@Autowired(required = false) UserDetailService userDetailService) {
            return new MemoryHmacClientService(userDetailService, securityProperties.hmac.getClients());
        }

        /**
         * HMAC认证处理过滤器
         * <p>
         * 创建HMAC认证的处理过滤器，负责验证API请求的HMAC签名。
         * 该过滤器会检查请求头中的签名信息，并使用HMAC-SHA算法进行验证。
         * </p>
         *
         * <h3>验证流程：</h3>
         * <ol>
         *   <li>提取请求中的AppId、时间戳、签名等信息</li>
         *   <li>根据AppId获取对应的Secret</li>
         *   <li>使用HMAC-SHA算法重新计算签名</li>
         *   <li>比较计算结果与请求签名</li>
         *   <li>验证时间戳防止重放攻击</li>
         * </ol>
         *
         * @param objectMapper      JSON序列化工具
         * @param hmacClientService HMAC客户端服务
         * @return HMAC认证过滤器注册Bean
         */
        @Bean
        public FilterRegistrationBean<HmacAuthenticationProcessingFilter> hmacAuthenticationProcessingFilter(
                ObjectMapper objectMapper, @Autowired(required = false) HmacClientService hmacClientService) {
            FilterRegistrationBean<HmacAuthenticationProcessingFilter> filterRegistrationBean =
                    new FilterRegistrationBean<>();
            HmacAuthenticationProcessingFilter processingFilter =
                    new HmacAuthenticationProcessingFilter(hmacClientService, new HmacShaEncoder());
            processingFilter.setAuthenticationSuccessHandler(new HmacAuthenticationSuccessHandler());
            processingFilter.setAuthenticationFailureHandler(new CommonAuthenticationFailureHandler(objectMapper));
            filterRegistrationBean.setFilter(processingFilter);
            filterRegistrationBean.addUrlPatterns("/*");
            filterRegistrationBean.setOrder(30);
            return filterRegistrationBean;
        }
    }

    /**
     * 表单登录配置类
     * <p>
     * 当启用表单登录功能时，提供传统的用户名+密码登录机制。
     * 支持验证码防护、登录锁定、密码加密等安全特性。
     * </p>
     *
     * <h3>核心功能：</h3>
     * <ul>
     *   <li><strong>用户名密码认证</strong> - 支持多种用户名格式（用户名、邮箱、手机号）</li>
     *   <li><strong>验证码防护</strong> - 可选的图形验证码防止暴力破解</li>
     *   <li><strong>登录锁定</strong> - 失败次数限制和账户锁定机制</li>
     *   <li><strong>密码加密</strong> - 使用标准密码编码器保护密码安全</li>
     *   <li><strong>登出管理</strong> - 完整的登出流程和会话清理</li>
     * </ul>
     *
     * <h3>安全特性：</h3>
     * <ul>
     *   <li>密码强度验证</li>
     *   <li>登录失败锁定</li>
     *   <li>会话管理</li>
     *   <li>CSRF防护</li>
     * </ul>
     */
    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "springboot properties")
    @Configuration
    @ConditionalOnProperty(prefix = "lambda.security.form", name = "enabled")
    public static class FormConfiguration {

        private SecurityProperties securityProperties;

        @Autowired
        public void setSecurityProperties(SecurityProperties securityProperties) {
            this.securityProperties = securityProperties;
        }

        /**
         * 表单登录锁定策略
         * <p>
         * 创建基于Redis的登录锁定策略，用于防止暴力破解攻击。
         * 当用户登录失败次数超过限制时，会锁定账户一段时间。
         * </p>
         *
         * <h3>锁定机制：</h3>
         * <ul>
         *   <li>失败次数统计</li>
         *   <li>自动锁定触发</li>
         *   <li>锁定时间控制</li>
         *   <li>分布式锁定支持</li>
         * </ul>
         *
         * @param stringRedisTemplate Redis字符串模板
         * @return 表单锁定策略实例
         */
        @Bean
        @ConditionalOnMissingBean
        public FormLockingStrategy securityLockingStrategy(StringRedisTemplate stringRedisTemplate) {
            SecurityProperties.Form.LockStrategy lockStrategy =
                    securityProperties.getForm().getLockStrategy();
            return new RedisLockingStrategy(
                    lockStrategy.getFailureMaxTimes(),
                    lockStrategy.getDuration(),
                    lockStrategy.getTimeUnit(),
                    stringRedisTemplate);
        }

        /**
         * 密码编码器
         * <p>
         * 创建标准密码编码器，用于密码的加密和验证。
         * 使用安全的哈希算法对用户密码进行加密存储。
         * </p>
         *
         * <h3>安全特性：</h3>
         * <ul>
         *   <li>单向哈希加密</li>
         *   <li>盐值随机生成</li>
         *   <li>防彩虹表攻击</li>
         *   <li>密码强度保护</li>
         * </ul>
         *
         * @return 密码编码器实例
         */
        @Bean
        @ConditionalOnMissingBean
        public PasswordEncoder passwordEncoder() {
            return new StandardPasswordEncoder();
        }

        /**
         * 表单认证处理过滤器
         * <p>
         * 创建表单登录的认证处理过滤器，负责处理用户名+密码的登录请求。
         * 集成了锁定策略、密码验证、用户详情查询等完整的认证流程。
         * </p>
         *
         * <h3>认证流程：</h3>
         * <ol>
         *   <li>接收用户名和密码</li>
         *   <li>检查账户锁定状态</li>
         *   <li>验证用户凭据</li>
         *   <li>密码匹配验证</li>
         *   <li>执行登录成功或失败处理</li>
         * </ol>
         *
         * @param formLockingStrategy 表单锁定策略
         * @param objectMapper        JSON序列化工具
         * @param passwordEncoder     密码编码器
         * @param userDetailService   用户详情服务
         * @return 表单认证过滤器注册Bean
         */
        @Bean
        public FilterRegistrationBean<FormAuthenticationProcessingFilter> defaultAuthenticationProcessingFilter(
                FormLockingStrategy formLockingStrategy,
                ObjectMapper objectMapper,
                PasswordEncoder passwordEncoder,
                @Autowired(required = false) List<FormLoginValidator> formLoginValidators,
                @Autowired(required = false) UserDetailService userDetailService) {
            FilterRegistrationBean<FormAuthenticationProcessingFilter> filterRegistrationBean =
                    new FilterRegistrationBean<>();
            FormAuthenticationProcessingFilter processingFilter =
                    new FormAuthenticationProcessingFilter(securityProperties.getForm().loginProcessingUrl);
            processingFilter.setFormLockingStrategy(formLockingStrategy);
            processingFilter.setAuthenticationSuccessHandler(new CommonAuthenticationSuccessHandler(objectMapper));
            processingFilter.setAuthenticationFailureHandler(new CommonAuthenticationFailureHandler(objectMapper));
            processingFilter.setFormLoginValidators(formLoginValidators);
            processingFilter.setUserDetailService(userDetailService);
            processingFilter.setPasswordEncoder(passwordEncoder);
            filterRegistrationBean.setFilter(processingFilter);
            filterRegistrationBean.addUrlPatterns("/*");
            filterRegistrationBean.setOrder(30);
            return filterRegistrationBean;
        }

        /**
         * 表单登出处理器
         * <p>
         * 创建表单登录的登出处理器，负责清理用户会话和相关资源。
         * 执行登出时的清理操作，确保用户安全退出。
         * </p>
         *
         * @return 登出处理器实例
         */
        @Bean
        public LogoutHandler formLogoutHandler() {
            return new CommonLogoutHandler();
        }

        /**
         * 表单登出成功处理器
         * <p>
         * 创建表单登录的登出成功处理器，负责处理登出成功后的响应。
         * 可以自定义登出成功后的跳转页面或返回数据。
         * </p>
         *
         * @return 登出成功处理器实例
         */
        @Bean
        @ConditionalOnMissingBean
        public LogoutSuccessHandler formLogoutSuccessHandler() {
            return new CommonLogoutSuccessHandler();
        }

        /**
         * 表单登出过滤器
         * <p>
         * 创建表单登录的登出过滤器，负责处理用户的登出请求。
         * 该过滤器会拦截登出URL，执行登出处理逻辑，并调用成功处理器。
         * </p>
         *
         * <h3>登出流程：</h3>
         * <ol>
         *   <li>拦截登出请求</li>
         *   <li>执行登出处理逻辑</li>
         *   <li>清理用户会话</li>
         *   <li>调用登出成功处理器</li>
         * </ol>
         *
         * @param formLogoutHandler        登出处理器
         * @param formLogoutSuccessHandler 登出成功处理器
         * @return 表单登出过滤器注册Bean
         */
        @Bean
        public FilterRegistrationBean<FormLogoutFilter> defaultLogoutFilter(
                LogoutHandler formLogoutHandler, LogoutSuccessHandler formLogoutSuccessHandler) {
            FilterRegistrationBean<FormLogoutFilter> filterRegistrationBean = new FilterRegistrationBean<>();
            FormLogoutFilter formLogoutFilter = new FormLogoutFilter(
                    securityProperties.getForm().getLoginProcessingUrl(), formLogoutSuccessHandler, formLogoutHandler);
            filterRegistrationBean.setFilter(formLogoutFilter);
            filterRegistrationBean.addUrlPatterns("/*");
            filterRegistrationBean.setOrder(40);
            return filterRegistrationBean;
        }
    }

    /**
     * 第三方登录配置类
     * <p>
     * 当启用第三方登录功能时，提供各种第三方平台的登录集成。
     * 支持微信小程序、微信公众号等主流第三方登录方式。
     * </p>
     *
     * <h3>支持的第三方平台：</h3>
     * <ul>
     *   <li><strong>微信小程序</strong> - 基于微信小程序的授权登录</li>
     *   <li><strong>微信公众号</strong> - 基于微信公众号的网页授权</li>
     *   <li><strong>扩展支持</strong> - 可扩展其他第三方平台</li>
     * </ul>
     *
     * <h3>核心特性：</h3>
     * <ul>
     *   <li>统一的第三方登录接口</li>
     *   <li>灵活的登录提供者机制</li>
     *   <li>自动用户信息同步</li>
     *   <li>安全的授权码验证</li>
     * </ul>
     */
    @SuppressFBWarnings(
            value = {"EI_EXPOSE_REP2"},
            justification = "springboot properties")
    @Configuration
    @ConditionalOnProperty(prefix = "lambda.security.thirdPartLogin", name = "enabled")
    public static class ThirdPartyConfiguration {

        private SecurityProperties securityProperties;

        @Autowired
        public void setSecurityProperties(SecurityProperties securityProperties) {
            this.securityProperties = securityProperties;
        }

        /**
         * 微信小程序登录配置类
         * <p>
         * 当启用微信小程序登录功能时，提供微信小程序的登录集成配置。
         * 基于微信小程序的code换取session_key机制实现用户身份验证。
         * </p>
         *
         * <h3>登录流程：</h3>
         * <ol>
         *   <li>小程序端调用wx.login()获取code</li>
         *   <li>将code发送到后端服务</li>
         *   <li>后端使用code换取session_key和openid</li>
         *   <li>验证用户身份并生成登录凭证</li>
         * </ol>
         *
         * <h3>安全特性：</h3>
         * <ul>
         *   <li>基于微信官方API</li>
         *   <li>session_key安全存储</li>
         *   <li>openid唯一标识</li>
         *   <li>Redis缓存支持</li>
         * </ul>
         */
        @Configuration
        @ConditionalOnProperty(prefix = "lambda.security.thirdPartLogin.wxMa", name = "enabled")
        public static class WxMaConfiguration {

            private SecurityProperties securityProperties;

            @Autowired
            public void setSecurityProperties(SecurityProperties securityProperties) {
                this.securityProperties = securityProperties;
            }

            /**
             * 微信小程序服务
             * <p>
             * 创建微信小程序API服务，用于与微信小程序后台进行交互。
             * 基于Redisson实现配置信息的分布式缓存存储。
             * </p>
             *
             * <h3>主要功能：</h3>
             * <ul>
             *   <li>code换取session_key</li>
             *   <li>获取用户openid</li>
             *   <li>access_token管理</li>
             *   <li>API调用封装</li>
             * </ul>
             *
             * @param redissonClient Redisson客户端
             * @return 微信小程序服务实例
             */
            @Bean
            public WxMaService wxMaService(RedissonClient redissonClient) {
                SecurityProperties.ThirdPartLogin thirdPartLogin = securityProperties.getThirdPartLogin();
                WxMaServiceImpl wxMaService = new WxMaServiceImpl();
                WxMaRedissonConfigImpl wxMaRedissonConfig = new WxMaRedissonConfigImpl(redissonClient);
                wxMaRedissonConfig.setAppid(thirdPartLogin.getWxMa().getAppId());
                wxMaRedissonConfig.setSecret(thirdPartLogin.getWxMa().getSecret());
                wxMaService.setWxMaConfig(wxMaRedissonConfig);
                return wxMaService;
            }

            /**
             * 微信小程序登录提供者
             * <p>
             * 创建微信小程序的登录提供者，负责处理微信小程序的登录逻辑。
             * 集成第三方登录服务、微信小程序API服务和登录处理器。
             * </p>
             *
             * <h3>处理流程：</h3>
             * <ol>
             *   <li>接收小程序登录code</li>
             *   <li>调用微信API换取用户信息</li>
             *   <li>执行用户登录处理逻辑</li>
             *   <li>返回登录结果</li>
             * </ol>
             *
             * @param thirdPartyLoginService 第三方登录服务
             * @param wxMaService            微信小程序服务
             * @param wxMaLoginHandler       微信小程序登录处理器
             * @return 微信小程序登录提供者
             */
            @Bean
            @ConditionalOnMissingBean
            public WxMaLoginProvider<WxMaLoginHandler> wxMaLoginProvider(
                    ThirdPartyLoginService thirdPartyLoginService,
                    WxMaService wxMaService,
                    WxMaLoginHandler wxMaLoginHandler) {
                return new WxMaLoginProvider<>(thirdPartyLoginService, wxMaService, wxMaLoginHandler);
            }

            /**
             * 微信小程序登录处理器
             * <p>
             * 创建微信小程序的登录处理器，定义具体的登录处理逻辑。
             * 可以通过自定义Bean来覆盖默认的处理行为。
             * </p>
             *
             * <h3>处理职责：</h3>
             * <ul>
             *   <li>用户信息处理</li>
             *   <li>登录状态管理</li>
             *   <li>业务逻辑集成</li>
             *   <li>响应数据构建</li>
             * </ul>
             *
             * @return 微信小程序登录处理器
             */
            @Bean
            @ConditionalOnMissingBean
            public WxMaLoginHandler wxMaLoginHandler() {
                return new WxMaLoginHandler() {};
            }
        }

        /**
         * 第三方认证处理过滤器
         * <p>
         * 创建第三方登录的认证处理过滤器，负责统一处理各种第三方平台的登录请求。
         * 支持多个第三方登录提供者，根据请求参数自动选择对应的处理器。
         * </p>
         *
         * <h3>处理机制：</h3>
         * <ul>
         *   <li>统一的第三方登录入口</li>
         *   <li>自动提供者选择</li>
         *   <li>标准化的认证流程</li>
         *   <li>统一的异常处理</li>
         * </ul>
         *
         * @param thirdPartLoginProviders 第三方登录提供者列表
         * @param objectMapper            JSON序列化工具
         * @return 第三方认证过滤器注册Bean
         * @throws IllegalStateException 当没有配置任何第三方登录提供者时
         */
        @Bean
        public FilterRegistrationBean<ThirdPartAuthenticationProcessingFilter> thirdPartAuthenticationFilter(
                @Autowired(required = false) List<ThirdPartLoginProvider> thirdPartLoginProviders,
                ObjectMapper objectMapper) {
            if (thirdPartLoginProviders == null || thirdPartLoginProviders.isEmpty()) {
                throw new IllegalStateException("thirdPartLoginProviders must not be empty");
            }
            FilterRegistrationBean<ThirdPartAuthenticationProcessingFilter> filterRegistrationBean =
                    new FilterRegistrationBean<>();
            ThirdPartAuthenticationProcessingFilter thirdPartAuthenticationProcessingFilter =
                    getThirdPartAuthenticationProcessingFilter(thirdPartLoginProviders, objectMapper);
            filterRegistrationBean.setFilter(thirdPartAuthenticationProcessingFilter);
            filterRegistrationBean.addUrlPatterns("/*");
            filterRegistrationBean.setOrder(30);
            return filterRegistrationBean;
        }

        /**
         * 创建第三方认证处理过滤器实例
         * <p>
         * 私有方法，用于创建和配置第三方认证处理过滤器。
         * 设置认证成功和失败的处理器，确保统一的响应格式。
         * </p>
         *
         * @param thirdPartLoginProviders 第三方登录提供者列表
         * @param objectMapper            JSON序列化工具
         * @return 配置完成的第三方认证处理过滤器
         */
        private ThirdPartAuthenticationProcessingFilter getThirdPartAuthenticationProcessingFilter(
                List<ThirdPartLoginProvider> thirdPartLoginProviders, ObjectMapper objectMapper) {
            ThirdPartAuthenticationProcessingFilter thirdPartAuthenticationProcessingFilter =
                    new ThirdPartAuthenticationProcessingFilter(
                            securityProperties.getThirdPartLogin(), thirdPartLoginProviders);
            thirdPartAuthenticationProcessingFilter.setAuthenticationSuccessHandler(
                    new CommonAuthenticationSuccessHandler(objectMapper));
            thirdPartAuthenticationProcessingFilter.setAuthenticationFailureHandler(
                    new CommonAuthenticationFailureHandler(objectMapper));
            return thirdPartAuthenticationProcessingFilter;
        }
    }
}
