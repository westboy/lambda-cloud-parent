package com.lambda.cloud.netty.protocol.config;

import lombok.Data;

/**
 * 协议引擎配置
 * <p>
 * 用于配置协议引擎的各种参数
 * </p>
 *
 * @author Jin
 */
@Data
public class ProtocolEngineConfig {

    /**
     * 是否启用性能监控
     */
    private boolean monitoringEnabled = true;

    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = true;

    /**
     * 字段缓存大小
     */
    private int fieldCacheSize = 1000;

    /**
     * 转换器缓存大小
     */
    private int converterCacheSize = 100;

    /**
     * ByteBuf池配置
     */
    private ByteBufPoolConfig byteBufPool = new ByteBufPoolConfig();

    /**
     * 验证配置
     */
    private ValidationConfig validation = new ValidationConfig();

    /**
     * ByteBuf池配置
     */
    @Data
    public static class ByteBufPoolConfig {
        /**
         * 小缓冲区大小（字节）
         */
        private int smallBufferSize = 256;

        /**
         * 中等缓冲区大小（字节）
         */
        private int mediumBufferSize = 1024;

        /**
         * 大缓冲区大小（字节）
         */
        private int largeBufferSize = 4096;

        /**
         * 每个池的最大容量
         */
        private int maxPoolCapacity = 100;
    }

    /**
     * 验证配置
     */
    @Data
    public static class ValidationConfig {
        /**
         * 是否启用严格验证
         */
        private boolean strictValidation = true;

        /**
         * 是否在验证失败时快速失败
         */
        private boolean failFast = true;

        /**
         * 最大验证错误数量
         */
        private int maxValidationErrors = 10;
    }

    /**
     * 创建默认配置
     *
     * @return 默认配置
     */
    public static ProtocolEngineConfig defaultConfig() {
        return new ProtocolEngineConfig();
    }

    /**
     * 创建高性能配置
     *
     * @return 高性能配置
     */
    public static ProtocolEngineConfig highPerformanceConfig() {
        ProtocolEngineConfig config = new ProtocolEngineConfig();
        config.setMonitoringEnabled(false);
        config.setCacheEnabled(true);
        config.setFieldCacheSize(2000);
        config.setConverterCacheSize(200);

        // 更大的ByteBuf池
        config.getByteBufPool().setMaxPoolCapacity(200);

        // 宽松的验证
        config.getValidation().setStrictValidation(false);
        config.getValidation().setFailFast(false);

        return config;
    }

    /**
     * 创建调试配置
     *
     * @return 调试配置
     */
    public static ProtocolEngineConfig debugConfig() {
        ProtocolEngineConfig config = new ProtocolEngineConfig();
        config.setMonitoringEnabled(true);
        config.setCacheEnabled(false); // 禁用缓存以便调试

        // 严格验证
        config.getValidation().setStrictValidation(true);
        config.getValidation().setFailFast(true);
        config.getValidation().setMaxValidationErrors(1);

        return config;
    }
}
