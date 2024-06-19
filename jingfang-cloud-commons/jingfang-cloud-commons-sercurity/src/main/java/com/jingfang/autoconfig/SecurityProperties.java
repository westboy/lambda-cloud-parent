package com.jingfang.autoconfig;

import cn.dev33.satoken.util.SaTokenConsts;
import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Data
@ConfigurationProperties(prefix = "spring.redis.redisson")
public class SecurityProperties {

    private static final List<String> defaultIgnorePathList = Stream.of("/public/**","*.html", "*.css", "*.js").collect(Collectors.toList());

    private String tokenName = "jf-token";
    private String tokenPrefix = "Bearer";
    private String tokenStyle = SaTokenConsts.TOKEN_STYLE_RANDOM_32;
    private Integer tokenTimeout = 30 * 24 * 60 * 60;
    /**
     * 设置是否打开注解鉴权：配置为 true 时注解鉴权才会生效，配置为 false 时，即使写了注解也不会进行鉴权
     */
    private Boolean enableAnnotationCheck;
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

    private List<String> ignorePaths;

    private Long activeTimeout = -1L;

    public List<String> getAllIgnoreList() {
        return CollUtil.addAllIfNotContains(defaultIgnorePathList, ignorePaths);
    }
}
