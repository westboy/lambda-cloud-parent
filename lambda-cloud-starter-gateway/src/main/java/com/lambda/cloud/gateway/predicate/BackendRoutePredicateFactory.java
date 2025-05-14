package com.lambda.cloud.gateway.predicate;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.http.server.PathContainer;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.springframework.http.server.PathContainer.parsePath;

/**
 * BackendRoutePredicateFactory
 *
 * @author jpjoo
 */
@Slf4j
public class BackendRoutePredicateFactory extends AbstractRoutePredicateFactory<BackendRoutePredicateFactory.Config> {
    private final PathPatternParser parser = new PathPatternParser();
    private static final String REGEX = ".*\\.(html|css|js)$";
    private final List<PathPattern> excludes = Lists.newArrayList(
            parser.parse("/static/**"),
            parser.parse("/config.js"),
            parser.parse("/index.html"),
            parser.parse("/favicon.ico"));

    public BackendRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Lists.newArrayList("includes");
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        final ArrayList<PathPattern> includes = new ArrayList<>();
        synchronized (parser) {
            parser.setMatchOptionalTrailingSeparator(true);
            config.getIncludes().forEach(pattern -> {
                PathPattern pathPattern = parser.parse(pattern);
                includes.add(pathPattern);
            });
        }
        return (GatewayPredicate) exchange -> {
            PathContainer path = parsePath(exchange.getRequest().getURI().getRawPath());
            if (CollectionUtils.isNotEmpty(includes)) {
                Optional<PathPattern> included = includes.stream().filter(pattern -> pattern.matches(path)).findFirst();
                if (included.isPresent()) {
                    return true;
                }
            }

            Optional<PathPattern> excluded = excludes.stream().filter(pattern -> pattern.matches(path)).findFirst();
            if (excluded.isPresent()) {
                return false;
            }
            return !Pattern.matches(REGEX, path.value());
        };
    }

    @Getter
    @Validated
    public static class Config {
        private List<String> includes = new ArrayList<>();

        public Config setIncludes(List<String> includes) {
            this.includes = includes;
            return this;
        }
    }
}
