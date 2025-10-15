package com.lambda.cloud.netty.protocol.engine;

import com.lambda.cloud.netty.protocol.core.MessageMetadata;
import com.lambda.cloud.netty.protocol.core.ProtocolEngine;
import com.lambda.cloud.netty.protocol.core.ProtocolException;
import com.lambda.cloud.netty.protocol.monitor.PerformanceMonitor;
import com.lambda.cloud.netty.protocol.pool.ByteBufPool;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 增强的协议引擎
 * <p>
 * 在基础协议引擎的基础上增加性能监控、资源管理等功能
 * </p>
 *
 * @author Jin
 */
@Slf4j
public class EnhancedProtocolEngine implements ProtocolEngine<Object> {

    /**
     * 委托的协议引擎
     */
    private final ProtocolEngine<Object> delegate;

    /**
     * 性能监控器
     * -- GETTER --
     *  获取性能监控器
     *
     * @return 性能监控器
     *
     */
    @Getter
    private final PerformanceMonitor performanceMonitor;

    /**
     * 是否启用性能监控
     */
    private final boolean monitoringEnabled;

    /**
     * 构造函数
     *
     * @param delegate           委托的协议引擎
     * @param monitoringEnabled  是否启用性能监控
     */
    public EnhancedProtocolEngine(ProtocolEngine<Object> delegate, boolean monitoringEnabled) {
        this.delegate = delegate;
        this.monitoringEnabled = monitoringEnabled;
        this.performanceMonitor = monitoringEnabled ? new PerformanceMonitor() : null;
    }

    /**
     * 构造函数（默认启用监控）
     *
     * @param delegate 委托的协议引擎
     */
    public EnhancedProtocolEngine(ProtocolEngine<Object> delegate) {
        this(delegate, true);
    }

    @Override
    public Object parse(ByteBuf byteBuf, Class<Object> messageClass) throws ProtocolException {
        if (!monitoringEnabled) {
            return delegate.parse(byteBuf, messageClass);
        }

        long startTime = System.nanoTime();
        boolean success = false;
        try {
            Object result = delegate.parse(byteBuf, messageClass);
            success = true;
            return result;
        } catch (ProtocolException e) {
            log.debug("解析消息失败: {}", e.getMessage());
            throw e;
        } finally {
            long duration = System.nanoTime() - startTime;
            performanceMonitor.recordParse(duration, success);
        }
    }

    @Override
    public void serialize(Object message, ByteBuf byteBuf) throws ProtocolException {
        if (!monitoringEnabled) {
            delegate.serialize(message, byteBuf);
            return;
        }

        long startTime = System.nanoTime();
        boolean success = false;
        try {
            delegate.serialize(message, byteBuf);
            success = true;
        } catch (ProtocolException e) {
            log.debug("序列化消息失败: {}", e.getMessage());
            throw e;
        } finally {
            long duration = System.nanoTime() - startTime;
            performanceMonitor.recordSerialize(duration, success);
        }
    }

    @Override
    public ValidationResult validate(Object message) {
        if (!monitoringEnabled) {
            return delegate.validate(message);
        }

        long startTime = System.nanoTime();
        boolean success = false;
        try {
            ValidationResult result = delegate.validate(message);
            success = result.valid();
            return result;
        } finally {
            long duration = System.nanoTime() - startTime;
            performanceMonitor.recordValidate(duration, success);
        }
    }

    @Override
    public int calculateLength(Class<?> messageClass) {
        return delegate.calculateLength(messageClass);
    }

    @Override
    public MessageMetadata getMetadata(Class<?> messageClass) {
        return delegate.getMetadata(messageClass);
    }

    /**
     * 获取性能统计信息
     *
     * @return 性能统计信息
     */
    public PerformanceMonitor.PerformanceStats getPerformanceStats() {
        return monitoringEnabled ? performanceMonitor.getStats() : null;
    }

    /**
     * 重置性能统计
     */
    public void resetPerformanceStats() {
        if (monitoringEnabled) {
            performanceMonitor.reset();
        }
    }

    /**
     * 创建优化的ByteBuf 用于序列化
     *
     * @param messageClass 消息类
     * @return ByteBuf 实例
     */
    public ByteBuf createOptimizedByteBuf(Class<Object> messageClass) {
        int length = calculateLength(messageClass);
        return ByteBufPool.acquire(length);
    }

    /**
     * 释放ByteBuf资源
     *
     * @param byteBuf 要释放的 ByteBuf
     */
    public void releaseByteBuf(ByteBuf byteBuf) {
        ByteBufPool.release(byteBuf);
    }

    /**
     * 获取资源使用统计
     *
     * @return 资源统计信息
     */
    public String getResourceStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Enhanced Protocol Engine Resource Statistics:\n");
        sb.append(ByteBufPool.getPoolStats());

        if (monitoringEnabled) {
            sb.append("\n").append(performanceMonitor.getStats());
        }

        return sb.toString();
    }
}
