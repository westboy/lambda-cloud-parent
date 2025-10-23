package com.lambda.cloud.netty.protocol.accessor.asm;

/**
 * Unsafe 信息封装
 */
public final class UnsafeInfo {
    final String className;
    final String internalName;
    final Class<?> clazz;

    UnsafeInfo(String className, Class<?> clazz) {
        this.className = className;
        this.internalName = className.replace('.', '/');
        this.clazz = clazz;
    }
}
