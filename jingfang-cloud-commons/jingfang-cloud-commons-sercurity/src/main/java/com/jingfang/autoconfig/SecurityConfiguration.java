package com.jingfang.autoconfig;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jingfang.security.handler.AuthenticationFailureHandler;
import com.jingfang.security.handler.AuthenticationSuccessHandler;
import com.jingfang.security.web.SecurityLockingStrategy;
import com.jingfang.security.web.authentication.DefaultAuthenticationProcessingFilter;
import com.jingfang.security.web.authentication.DefaultLogoutFilter;
import com.jingfang.security.web.authentication.handler.DefaultAuthenticationFailureHandler;
import com.jingfang.security.web.authentication.handler.DefaultAuthenticationSuccessHandler;
import com.jingfang.security.web.authentication.handler.DefaultLogoutHandler;
import com.jingfang.security.web.authentication.handler.DefaultLogoutSuccessHandler;
import com.jingfang.security.web.authentication.locking.RedisLockingStrategy;
import com.jingfang.security.web.verify.VerifyCodeFilter;
import com.jingfang.security.web.verify.service.CaptchaVerifyCodeGenerateImpl;
import com.jingfang.security.web.verify.service.CaptchaVerifyCodeValidationImpl;
import com.jingfang.security.web.verify.service.VerifyCodeService;
import com.jingfang.security.web.verify.store.CaptchaStore;
import com.jingfang.security.web.verify.store.RedisCaptchaStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Sa-Token 配置类
 */
@Configuration
@EnableConfigurationProperties({SecurityProperties.class})
public class SecurityConfiguration {

    private SecurityProperties securityProperties;

    @Autowired
    public void setSecurityProperties(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    @Primary
    public SaTokenConfig getSaTokenConfigPrimary() {
        StpUtil.login(10001);
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
                return new SaInterceptor(handler -> StpUtil.checkLogin()).isAnnotation(securityProperties.getEnableMethodAnnotation());
            }
        };
    }

    @Bean
    public SecurityLockingStrategy securityLockingStrategy() {
        SecurityProperties.Form.LockStrategy lockStrategy = securityProperties.getForm().getLockStrategy();
        int times = lockStrategy.getFailureMaxTimes();
        int duration = lockStrategy.getDuration();
        TimeUnit timeUnit = lockStrategy.getTimeUnit();
        return new RedisLockingStrategy(times, duration, timeUnit);
    }



    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler(ObjectMapper objectMapper) {
        return new DefaultAuthenticationFailureHandler(objectMapper);
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(ObjectMapper objectMapper) {
        return new DefaultAuthenticationSuccessHandler(objectMapper);
    }

    @Bean
    public DefaultAuthenticationProcessingFilter defaultAuthenticationProcessingFilter(SecurityLockingStrategy securityLockingStrategy,
                                                                                       AuthenticationFailureHandler authenticationFailureHandler,
                                                                                       AuthenticationSuccessHandler authenticationSuccessHandler
                                                                                       ) {
        DefaultAuthenticationProcessingFilter processingFilter = new DefaultAuthenticationProcessingFilter(securityProperties.getForm().loginProcessingUrl);
        processingFilter.setSecurityLockingStrategy(securityLockingStrategy);
        processingFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        processingFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        return processingFilter;
    }

    @Bean
    public DefaultLogoutHandler defaultLogoutHandler() {
        return new DefaultLogoutHandler();
    }

    @Bean
    public DefaultLogoutSuccessHandler defaultLogoutSuccessHandler() {
        return new DefaultLogoutSuccessHandler();
    }

    @Bean
    public DefaultLogoutFilter defaultLogoutFilter(DefaultLogoutHandler defaultLogoutHandler, DefaultLogoutSuccessHandler defaultLogoutSuccessHandler) {
        return new DefaultLogoutFilter(securityProperties.getForm().getLoginProcessingUrl(), defaultLogoutSuccessHandler, defaultLogoutHandler);
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
    public VerifyCodeFilter verifyCodeFilter(List<VerifyCodeService> verifyCodeServices) {
        return new VerifyCodeFilter(verifyCodeServices);
    }
}
