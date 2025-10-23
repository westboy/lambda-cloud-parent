package com.lambda.cloud.netty.protocol.accessor;

/**
 * 访问器类型枚举
 */
public enum FileAccessorType {
    /**
     * 基于 MethodHandle 的反射访问器
     * 兼容性好，性能中等
     */
    REFLECTION,

    /**
     * 基于 ASM 字节码生成的访问器
     * 性能最高，但有一定的内存开销
     */
    BYTECODE
}