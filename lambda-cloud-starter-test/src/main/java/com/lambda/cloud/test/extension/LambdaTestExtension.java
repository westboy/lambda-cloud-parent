package com.lambda.cloud.test.extension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * Lambda Cloud 测试扩展类。
 * <p>
 * 该扩展类为 Lambda Cloud 项目的测试提供统一的生命周期管理和通用功能，
 * 包括测试执行时间统计、日志记录、资源清理等。
 * <p>
 * 主要功能：
 * <ul>
 *     <li>测试执行时间统计</li>
 *     <li>测试结果监控和日志记录</li>
 *     <li>测试前后的资源管理</li>
 *     <li>测试上下文信息收集</li>
 * </ul>
 *
 * @author Jin
 * @since 1.0.0
 */
@SuppressWarnings("all")
@Slf4j
public class LambdaTestExtension
        implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, TestWatcher {

    private static final String START_TIME_KEY = "lambda.test.start.time";
    private static final String CLASS_START_TIME_KEY = "lambda.test.class.start.time";

    @Override
    public void beforeAll(ExtensionContext context) {
        Instant startTime = Instant.now();
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(CLASS_START_TIME_KEY, startTime);

        log.info("开始执行测试类: {}", context.getDisplayName());
    }

    @Override
    public void afterAll(ExtensionContext context) {
        Instant startTime =
                context.getStore(ExtensionContext.Namespace.GLOBAL).get(CLASS_START_TIME_KEY, Instant.class);

        if (startTime != null) {
            Duration duration = Duration.between(startTime, Instant.now());
            log.info("测试类 {} 执行完成，总耗时: {} ms", context.getDisplayName(), duration.toMillis());
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        Instant startTime = Instant.now();
        context.getStore(ExtensionContext.Namespace.GLOBAL).put(START_TIME_KEY, startTime);

        log.debug("开始执行测试方法: {}", context.getDisplayName());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Instant startTime = context.getStore(ExtensionContext.Namespace.GLOBAL).get(START_TIME_KEY, Instant.class);

        if (startTime != null) {
            Duration duration = Duration.between(startTime, Instant.now());
            log.debug("测试方法 {} 执行完成，耗时: {} ms", context.getDisplayName(), duration.toMillis());
        }
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        log.info("✅ 测试通过: {}", context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        log.error("❌ 测试失败: {} - 原因: {}", context.getDisplayName(), cause.getMessage(), cause);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        log.warn("⚠️ 测试中止: {} - 原因: {}", context.getDisplayName(), cause.getMessage());
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        log.info("⏭️ 测试跳过: {} - 原因: {}", context.getDisplayName(), reason.orElse("未指定原因"));
    }
}
