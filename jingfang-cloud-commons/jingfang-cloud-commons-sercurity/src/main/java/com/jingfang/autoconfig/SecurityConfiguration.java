package com.jingfang.autoconfig;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


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
                return new SaInterceptor(handler -> StpUtil.checkLogin()).isAnnotation(securityProperties.getEnableAnnotationCheck());
            }
        };
    }
}
