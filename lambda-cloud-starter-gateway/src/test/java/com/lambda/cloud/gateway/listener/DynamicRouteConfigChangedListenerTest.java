package com.lambda.cloud.gateway.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.env.MockEnvironment;
import reactor.core.publisher.Mono;

class DynamicRouteConfigChangedListenerTest {

    @Test
    void refreshRoutesClearsExistingRoutesWhenConfigBecomesBlank() {
        MockEnvironment environment = new MockEnvironment();
        RecordingRouteDefinitionWriter writer = new RecordingRouteDefinitionWriter();
        RecordingEventPublisher publisher = new RecordingEventPublisher();
        DynamicRouteConfigChangedListener listener =
                new DynamicRouteConfigChangedListener(environment, writer, publisher, new ObjectMapper());

        environment.setProperty(
                DynamicRouteConfigChangedListener.DYNAMIC_ROUTES_KEY,
                """
                [
                  {
                    "id": "demo-route",
                    "uri": "https://example.com",
                    "predicates": [
                      { "name": "Path", "args": { "_genkey_0": "/demo/**" } }
                    ]
                  }
                ]
                """);
        listener.refreshRoutes("test-load");

        environment.setProperty(DynamicRouteConfigChangedListener.DYNAMIC_ROUTES_KEY, " ");
        listener.refreshRoutes("test-clear");

        assertEquals(List.of("demo-route"), writer.savedRouteIds);
        assertEquals(List.of("demo-route"), writer.deletedRouteIds);
        assertEquals(2, publisher.refreshEventCount);
    }

    @Test
    void refreshRoutesReplacesExistingRoutesBeforePublishingRefreshEvent() {
        MockEnvironment environment = new MockEnvironment();
        RecordingRouteDefinitionWriter writer = new RecordingRouteDefinitionWriter();
        RecordingEventPublisher publisher = new RecordingEventPublisher();
        DynamicRouteConfigChangedListener listener =
                new DynamicRouteConfigChangedListener(environment, writer, publisher, new ObjectMapper());

        environment.setProperty(
                DynamicRouteConfigChangedListener.DYNAMIC_ROUTES_KEY,
                """
                [
                  {
                    "id": "route-a",
                    "uri": "https://example.com",
                    "predicates": [
                      { "name": "Path", "args": { "_genkey_0": "/a/**" } }
                    ]
                  }
                ]
                """);
        listener.refreshRoutes("test-initial");

        environment.setProperty(
                DynamicRouteConfigChangedListener.DYNAMIC_ROUTES_KEY,
                """
                [
                  {
                    "id": "route-b",
                    "uri": "https://example.org",
                    "predicates": [
                      { "name": "Path", "args": { "_genkey_0": "/b/**" } }
                    ]
                  }
                ]
                """);
        listener.refreshRoutes("test-replace");

        assertEquals(List.of("route-a", "route-b"), writer.savedRouteIds);
        assertEquals(List.of("route-a"), writer.deletedRouteIds);
        assertEquals(2, publisher.refreshEventCount);
    }

    private static final class RecordingRouteDefinitionWriter implements RouteDefinitionWriter {

        private final List<String> savedRouteIds = new ArrayList<>();
        private final List<String> deletedRouteIds = new ArrayList<>();

        @Override
        public Mono<Void> save(Mono<RouteDefinition> route) {
            return route.doOnNext(definition -> savedRouteIds.add(definition.getId())).then();
        }

        @Override
        public Mono<Void> delete(Mono<String> routeId) {
            return routeId.doOnNext(deletedRouteIds::add).then();
        }
    }

    private static final class RecordingEventPublisher implements ApplicationEventPublisher {

        private int refreshEventCount;

        @Override
        public void publishEvent(ApplicationEvent event) {
            if (event instanceof RefreshRoutesEvent) {
                refreshEventCount++;
            }
        }

        @Override
        public void publishEvent(Object event) {
            if (event instanceof RefreshRoutesEvent) {
                refreshEventCount++;
            }
        }
    }
}
