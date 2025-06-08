package com.lambda.autoconfig;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaRedissonConfigImpl;
import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.same.SaSameUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.cloud.core.exception.model.ErrorModel;
import com.lambda.cloud.core.utils.Assert;
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
import com.lambda.security.provider.impl.WxMaLoginProvider;
import com.lambda.security.service.HmacClientService;
import com.lambda.security.service.ThirdPartyLoginService;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.form.FormAuthenticationProcessingFilter;
import com.lambda.security.web.form.FormLockingStrategy;
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
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * Sa-Token 配置类
 *
 * @author jpjoo
 */
@Slf4j
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@EnableConfigurationProperties({SecurityProperties.class})
public class SecurityAutoConfiguration {

    public SecurityAutoConfiguration() {
        log.trace("initializing...");
    }

    @Bean
    @ConditionalOnProperty(prefix = "lambda.security.xss-protected", name = "enabled")
    public XSSDefendFilter xssDefendFilter(SecurityProperties securityProperties) {
        SecurityProperties.XssProtected xssProtected = securityProperties.getXssProtected();
        return new XSSDefendFilter(xssProtected.trusted);
    }

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

        @Bean
        @Primary
        @ConfigurationProperties(prefix = "lambda.security.sa-token")
        public SaTokenConfig getSaTokenConfig() {
            return new SaTokenConfig();
        }

        @Bean
        @ConditionalOnBean(SecureInterceptor.class)
        public SaInterceptor saInterceptor(SecureInterceptor secureInterceptor) {
            return new SaInterceptor(new SaTokenInterceptor(secureInterceptor))
                    .isAnnotation(securityProperties.getSaToken().getEnableMethodAuthentication());
        }

        @Bean
        @ConditionalOnBean(SaInterceptor.class)
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

        @Bean
        @ConditionalOnProperty(prefix = "lambda.security.sa-token", name = "check-same-token")
        public SaServletFilter getSaServletFilter() {
            return new SaServletFilter()
                    .addInclude("/**")
                    .addExclude(
                            securityProperties.getSaToken().getAllIgnoreList().toArray(new String[0]))
                    .setAuth(e -> {
                        HttpServletRequest currentRequest = WebHttpUtils.getCurrentRequest();
                        boolean hmacRequest = WebHttpUtils.isHmacRequest(currentRequest);
                        if (!hmacRequest) {
                            SaSameUtil.checkCurrentRequestToken();
                        }
                    })
                    .setError(e -> {
                        ErrorModel errorModel = new ErrorModel();
                        errorModel.setStatus(HttpStatus.UNAUTHORIZED.value());
                        errorModel.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
                        errorModel.setTimestamp(System.currentTimeMillis());
                        errorModel.setMessage(e.getMessage());
                        return errorModel.toJsonString();
                    });
        }
    }

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

        @Bean
        public CaptchaStore redisCaptchaStore(RedisHelper redisHelper) {
            RedisCaptchaStore redisCaptchaStore = new RedisCaptchaStore();
            redisCaptchaStore.setRedisHelper(redisHelper);
            return redisCaptchaStore;
        }

        @Bean
        public VerifyCodeService captchaVerifyCodeGenerate(ObjectMapper objectMapper, CaptchaStore redisCaptchaStore) {
            return new CaptchaVerifyCodeGenerateImpl(securityProperties, objectMapper, redisCaptchaStore);
        }

        @Bean
        public VerifyCodeService captchaVerifyCodeValidation(CaptchaStore redisCaptchaStore) {
            return new CaptchaVerifyCodeValidationImpl(securityProperties, redisCaptchaStore);
        }

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

        @Bean
        public SmsVerifyCodeStore<String> smsVerifyCodeStore(StringRedisTemplate stringRedisTemplate) {
            return new RedisSmsVerifyCodeStore(securityProperties.getSms(), stringRedisTemplate);
        }

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

        @Bean
        public VerifyCodeService smsVerifyCodeValidation(SmsVerifyCodeStore<String> smsVerifyCodeStore) {
            return new SmsVerifyCodeValidationImpl(securityProperties, smsVerifyCodeStore);
        }
    }

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

        @Bean
        @ConditionalOnMissingBean
        public HmacClientService hmacClientService(@Autowired(required = false) UserDetailService userDetailService) {
            return new MemoryHmacClientService(userDetailService, securityProperties.hmac.getClients());
        }

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

        @Bean
        @ConditionalOnMissingBean
        public PasswordEncoder passwordEncoder() {
            return new StandardPasswordEncoder();
        }

        @Bean
        public FilterRegistrationBean<FormAuthenticationProcessingFilter> defaultAuthenticationProcessingFilter(
                FormLockingStrategy formLockingStrategy,
                ObjectMapper objectMapper,
                PasswordEncoder passwordEncoder,
                @Autowired(required = false) UserDetailService userDetailService) {
            FilterRegistrationBean<FormAuthenticationProcessingFilter> filterRegistrationBean =
                    new FilterRegistrationBean<>();
            FormAuthenticationProcessingFilter processingFilter =
                    new FormAuthenticationProcessingFilter(securityProperties.getForm().loginProcessingUrl);
            processingFilter.setFormLockingStrategy(formLockingStrategy);
            processingFilter.setAuthenticationSuccessHandler(new CommonAuthenticationSuccessHandler(objectMapper));
            processingFilter.setAuthenticationFailureHandler(new CommonAuthenticationFailureHandler(objectMapper));
            processingFilter.setUserDetailService(userDetailService);
            processingFilter.setPasswordEncoder(passwordEncoder);
            filterRegistrationBean.setFilter(processingFilter);
            filterRegistrationBean.addUrlPatterns("/*");
            filterRegistrationBean.setOrder(30);
            return filterRegistrationBean;
        }

        @Bean
        public LogoutHandler formLogoutHandler() {
            return new CommonLogoutHandler();
        }

        @Bean
        @ConditionalOnMissingBean
        public LogoutSuccessHandler formLogoutSuccessHandler() {
            return new CommonLogoutSuccessHandler();
        }

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

        @Configuration
        @ConditionalOnProperty(prefix = "lambda.security.thirdPartLogin.wxMa", name = "enabled")
        public static class WxMaConfiguration {

            private SecurityProperties securityProperties;

            @Autowired
            public void setSecurityProperties(SecurityProperties securityProperties) {
                this.securityProperties = securityProperties;
            }

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

            @Bean
            @ConditionalOnBean(ThirdPartLoginProvider.class)
            public WxMaLoginProvider wxMaLoginProvider(
                    ThirdPartyLoginService thirdPartyLoginService, WxMaService wxMaService) {
                return new WxMaLoginProvider(thirdPartyLoginService, wxMaService);
            }
        }

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
                    new ThirdPartAuthenticationProcessingFilter(
                            securityProperties.getThirdPartLogin(), thirdPartLoginProviders);
            thirdPartAuthenticationProcessingFilter.setAuthenticationSuccessHandler(
                    new CommonAuthenticationSuccessHandler(objectMapper));
            thirdPartAuthenticationProcessingFilter.setAuthenticationFailureHandler(
                    new CommonAuthenticationFailureHandler(objectMapper));
            filterRegistrationBean.setFilter(thirdPartAuthenticationProcessingFilter);
            filterRegistrationBean.addUrlPatterns("/*");
            filterRegistrationBean.setOrder(30);
            return filterRegistrationBean;
        }
    }
}
