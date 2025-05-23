package com.lambda.cloud.gateway.filter.factory;

import com.lambda.cloud.gateway.utils.WebFluxUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * 黑名单过滤器
 *
 * @author westboy
 */
@Component
public class BlackListUrlFilterFactory extends AbstractGatewayFilterFactory<BlackListUrlFilterFactory.Config> {
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String url = exchange.getRequest().getURI().getPath();
            if (config.matchBlacklist(url)) {
                return WebFluxUtils.webFluxResponseWriter(
                        exchange.getResponse(), url, HttpStatus.UNAUTHORIZED, "请求地址不允许访问");
            }

            return chain.filter(exchange);
        };
    }

    public BlackListUrlFilterFactory() {
        super(Config.class);
    }

    @Data
    @SuppressFBWarnings(value = "EI_EXPOSE_REP")
    public static class Config {
        private List<String> blacklistUrl;

        private List<Pattern> blacklistUrlPattern = new ArrayList<>();

        public boolean matchBlacklist(String url) {
            return !blacklistUrlPattern.isEmpty()
                    && blacklistUrlPattern.stream().anyMatch(p -> p.matcher(url).find());
        }

        public void setBlacklistUrl(List<String> blacklistUrl) {
            this.blacklistUrl = blacklistUrl;
            this.blacklistUrlPattern.clear();
            this.blacklistUrl.forEach(url -> {
                this.blacklistUrlPattern.add(
                        Pattern.compile(url.replaceAll("\\*\\*", "(.*?)"), Pattern.CASE_INSENSITIVE));
            });
        }
    }
}
