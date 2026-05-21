package com.lambda.cloud.gateway.listener;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class DynamicRouteConfigChangedListener {

    static final String DYNAMIC_ROUTES_KEY = "lambda.gateway.dynamic-routes";

    private final Environment environment;
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    private final ReentrantLock refreshLock = new ReentrantLock();
    private final List<String> currentRouteIds = new ArrayList<>();

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        refreshRoutes("application-ready");
    }

    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefreshScopeRefreshed() {
        refreshRoutes("refresh-scope");
    }

    void refreshRoutes(String source) {
        if (!refreshLock.tryLock()) {
            log.debug("Dynamic route refresh already in progress, source: {}", source);
            return;
        }
        try {
            String routesJson = environment.getProperty(DYNAMIC_ROUTES_KEY);
            if (routesJson == null || routesJson.trim().isEmpty()) {
                clearCurrentRoutes();
                log.info("Dynamic routes cleared, source: {}", source);
                return;
            }

            List<RouteDefinition> routeDefinitions =
                    objectMapper.readValue(routesJson, new TypeReference<List<RouteDefinition>>() {});
            replaceCurrentRoutes(routeDefinitions);
            log.info("Dynamic routes refreshed successfully, count: {}, source: {}", routeDefinitions.size(), source);
        } catch (Exception e) {
            log.error("Failed to refresh dynamic routes, source: {}", source, e);
        } finally {
            refreshLock.unlock();
        }
    }

    private void replaceCurrentRoutes(List<RouteDefinition> routeDefinitions) {
        clearRouteDefinitions();
        for (RouteDefinition routeDefinition : routeDefinitions) {
            routeDefinitionWriter.save(Mono.just(routeDefinition)).block();
            currentRouteIds.add(routeDefinition.getId());
        }
        publishRefreshEvent();
    }

    private void clearCurrentRoutes() {
        if (currentRouteIds.isEmpty()) {
            return;
        }
        clearRouteDefinitions();
        publishRefreshEvent();
    }

    private void clearRouteDefinitions() {
        List<String> routeIds = new ArrayList<>(currentRouteIds);
        currentRouteIds.clear();
        for (String routeId : routeIds) {
            routeDefinitionWriter.delete(Mono.just(routeId)).onErrorResume(ex -> {
                log.warn("Failed to delete dynamic route: {}", routeId, ex);
                return Mono.empty();
            }).block();
        }
    }

    private void publishRefreshEvent() {
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }
}
