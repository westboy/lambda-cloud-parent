package com.lambda.cloud.actuator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.*;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

/**
 * @author jin
 */
@Slf4j
@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "MeterRegistry is thread-safe and designed for shared access")
public record MeterHelper(MeterRegistry registry) {

    public Counter counter(@Nonnull String name, @Nonnull String description) {
        return counter(name, description, new String[]{});
    }

    public Counter counter(@Nonnull String name, @Nonnull String description, String... tags) {
        Counter.Builder builder = Counter.builder(name).description(description);
        if (ArrayUtils.isNotEmpty(tags)) {
            builder.tags(tags);
        }
        return builder.register(registry);
    }

    public Counter counter(@Nonnull String name, @Nonnull String description, Map<String, String> tags) {
        Counter.Builder builder = Counter.builder(name).description(description);
        if (tags != null && !tags.isEmpty()) {
            tags.forEach(builder::tag);
        }
        return builder.register(registry);
    }

    public <T> Gauge gauge(@Nonnull String name, @Nonnull String description, T object, ToDoubleFunction<T> function) {
        return gauge(name, description, null, object, function);
    }

    public <T> Gauge gauge(
            @Nonnull String name,
            @Nonnull String description,
            Map<String, String> tags,
            T object,
            ToDoubleFunction<T> function) {
        Gauge.Builder<T> builder = Gauge.builder(name, object, function).description(description);
        if (tags != null && !tags.isEmpty()) {
            tags.forEach(builder::tag);
        }
        return builder.register(registry);
    }

    public <T extends Number> Gauge gauge(@Nonnull String name, @Nonnull String description, T number) {
        return gauge(name, description, null, number);
    }

    public <T extends Number> Gauge gauge(
            @Nonnull String name, @Nonnull String description, Map<String, String> tags, T number) {
        Gauge.Builder<Supplier<T>> builder = Gauge.builder(name, () -> number).description(description);
        if (tags != null && !tags.isEmpty()) {
            tags.forEach(builder::tag);
        }
        return builder.register(registry);
    }

    public <T> MultiGauge multiGauge(@Nonnull String name, @Nonnull String description) {
        return multiGauge(name, description, null);
    }

    public MultiGauge multiGauge(@Nonnull String name, @Nonnull String description, Map<String, String> tags) {
        MultiGauge.Builder builder = MultiGauge.builder(name).description(description);
        if (tags != null && !tags.isEmpty()) {
            tags.forEach(builder::tag);
        }
        return builder.register(registry);
    }

    public Timer timer(@Nonnull String name, @Nonnull String description, Map<String, String> tags) {
        Timer.Builder builder = Timer.builder(name).description(description);
        if (tags != null && !tags.isEmpty()) {
            tags.forEach(builder::tag);
        }
        return builder.register(registry);
    }
}
