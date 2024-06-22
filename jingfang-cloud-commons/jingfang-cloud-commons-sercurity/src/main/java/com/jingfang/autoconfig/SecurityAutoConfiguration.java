package com.jingfang.autoconfig;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpLogic;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingfang.security.enums.LoginType;
import com.jingfang.security.handler.AuthenticationFailureHandler;
import com.jingfang.security.handler.AuthenticationSuccessHandler;
import com.jingfang.security.inteceptor.SecureExtendInterceptor;
import com.jingfang.security.inteceptor.SecureInterceptor;
import com.jingfang.security.password.StandardPasswordEncoder;
import com.jingfang.security.service.UserDetailService;
import com.jingfang.security.web.SecurityLockingStrategy;
import com.jingfang.security.web.authentication.DefaultAuthenticationProcessingFilter;
import com.jingfang.security.web.authentication.DefaultLogoutFilter;
import com.jingfang.security.web.authentication.handler.DefaultAuthenticationFailureHandler;
import com.jingfang.security.web.authentication.handler.DefaultAuthenticationSuccessHandler;
import com.jingfang.security.web.authentication.handler.DefaultLogoutHandler;
import com.jingfang.security.web.authentication.handler.DefaultLogoutSuccessHandler;
import com.jingfang.security.web.authentication.locking.RedisLockingStrategy;
import com.jingfang.security.web.verify.VerifyCodeFilter;
import com.jingfang.security.web.verify.service.captcha.CaptchaVerifyCodeGenerateImpl;
import com.jingfang.security.web.verify.service.captcha.CaptchaVerifyCodeValidationImpl;
import com.jingfang.security.web.verify.service.VerifyCodeService;
import com.jingfang.security.web.verify.store.CaptchaStore;
import com.jingfang.security.web.verify.store.RedisCaptchaStore;
import com.jingfang.security.web.xss.XSSDefendFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.List;


/**
 * Sa-Token 配置类
 *
 * @author jpjoo
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({SecurityProperties.class})
public class SecurityAutoConfiguration {

    public SecurityAutoConfiguration() {
        log.trace("initializing...");
    }

    private SecurityProperties securityProperties;
    private SecureExtendInterceptor secureExtendInterceptor;
    private UserDetailService userDetailService;

    @Autowired(required = false)
    public void setSaTokenCustomHandler(SecureExtendInterceptor secureExtendInterceptor) {
        this.secureExtendInterceptor = secureExtendInterceptor;
    }
    @Autowired
    public void setSecurityProperties(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Autowired(required = false)
    public void setUserDetailService(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Bean
    @Primary
    public SaTokenConfig getSaTokenConfigPrimary() {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenName(securityProperties.getTokenName());
        config.setTokenStyle(securityProperties.getTokenStyle());
        config.setTokenPrefix(securityProperties.getTokenPrefix());
        config.setTimeout(securityProperties.getTokenTimeout());
        config.setActiveTimeout(securityProperties.getActiveTimeout());
        config.setIsConcurrent(!securityProperties.getEnableKickOut());
        config.setIsShare(securityProperties.getEnableTokenShare());
        config.setIsLog(securityProperties.getEnableLogPrint());
        config.setJwtSecretKey(securityProperties.getJwtSecretKey());
        return config;
    }

    @Bean
    @ConditionalOnProperty(prefix = "jf.security.xss-protected", name = "enabled")
    public XSSDefendFilter xssDefendFilter(SecurityProperties securityProperties) {
        SecurityProperties.XssProtected xssProtected = securityProperties.getXssProtected();
        return new XSSDefendFilter(xssProtected.trusted);
    }

    @Bean
    @Primary
    public WebMvcConfigurer saInterceptorWebConfigurer() {
        return new WebMvcConfigurer() {

            @SuppressWarnings("NullableProblems")
            @Override
            public void addInterceptors(InterceptorRegistry interceptorRegistry) {
                SaInterceptor saInterceptor = getSaInterceptor();
                interceptorRegistry
                        .addInterceptor(saInterceptor)
                        .addPathPatterns("/**")
                        .excludePathPatterns(securityProperties.getAllIgnoreList());
            }

            private SaInterceptor getSaInterceptor() {
                return new SaInterceptor(new SecureInterceptor(userDetailService, secureExtendInterceptor)).isAnnotation(securityProperties.getEnableMethodAnnotation());
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityLockingStrategy securityLockingStrategy(StringRedisTemplate stringRedisTemplate) {
        SecurityProperties.Form.LockStrategy lockStrategy = securityProperties.getForm().getLockStrategy();
        return new RedisLockingStrategy(lockStrategy.getFailureMaxTimes(), lockStrategy.getDuration(), lockStrategy.getTimeUnit(), stringRedisTemplate);
    }


    @Bean
    @ConditionalOnMissingBean
    public AuthenticationFailureHandler authenticationFailureHandler(ObjectMapper objectMapper) {
        return new DefaultAuthenticationFailureHandler(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthenticationSuccessHandler authenticationSuccessHandler(ObjectMapper objectMapper) {
        return new DefaultAuthenticationSuccessHandler(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new StandardPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<DefaultAuthenticationProcessingFilter> defaultAuthenticationProcessingFilter(SecurityLockingStrategy securityLockingStrategy,
                                                                                                               AuthenticationFailureHandler authenticationFailureHandler,
                                                                                                               AuthenticationSuccessHandler authenticationSuccessHandler,
                                                                                                               PasswordEncoder passwordEncoder,
                                                                                                               @Autowired(required = false) UserDetailService userDetailService
    ) {
        FilterRegistrationBean<DefaultAuthenticationProcessingFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        DefaultAuthenticationProcessingFilter processingFilter = new DefaultAuthenticationProcessingFilter(securityProperties.getForm().loginProcessingUrl);
        processingFilter.setSecurityLockingStrategy(securityLockingStrategy);
        processingFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        processingFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        processingFilter.setUserDetailService(userDetailService);
        processingFilter.setPasswordEncoder(passwordEncoder);
        filterRegistrationBean.setFilter(processingFilter);
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(30);
        return filterRegistrationBean;
    }

    @Bean
    public DefaultLogoutHandler defaultLogoutHandler() {
        return new DefaultLogoutHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultLogoutSuccessHandler defaultLogoutSuccessHandler() {
        return new DefaultLogoutSuccessHandler();
    }

    @Bean
    public FilterRegistrationBean<DefaultLogoutFilter> defaultLogoutFilter(DefaultLogoutHandler defaultLogoutHandler, DefaultLogoutSuccessHandler defaultLogoutSuccessHandler) {
        FilterRegistrationBean<DefaultLogoutFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        DefaultLogoutFilter defaultLogoutFilter = new DefaultLogoutFilter(securityProperties.getForm().getLoginProcessingUrl(), defaultLogoutSuccessHandler, defaultLogoutHandler);
        filterRegistrationBean.setFilter(defaultLogoutFilter);
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(40);
        return filterRegistrationBean;
    }


    @Bean
    public CaptchaStore redisCaptchaStore() {
        return new RedisCaptchaStore();
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
    public FilterRegistrationBean<VerifyCodeFilter> verifyCodeFilter(List<VerifyCodeService> verifyCodeServices, AuthenticationFailureHandler authenticationFailureHandler) {
        FilterRegistrationBean<VerifyCodeFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        VerifyCodeFilter verifyCodeFilter = new VerifyCodeFilter(verifyCodeServices);
        verifyCodeFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        filterRegistrationBean.setFilter(verifyCodeFilter);
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(20);
        return filterRegistrationBean;
    }
}
