package com.lambda.cloud.netty.protocol.packet;

/**
 * 溢出处理策略
 */
public enum OverflowStrategy {
    /**
     * 截断超出部分
     */
    TRUNCATE,
    /**
     * 饱和到最大值
     */
    SATURATE,
    /**
     * 抛出异常
     */
    THROW
}
