package com.lambda.cloud.netty.protocol.annotation;

/**
 * 填充方向枚举
 * <p>
 * 定义数据填充的方向
 * </p>
 *
 */
public enum PaddingDirection {

    /**
     * 左填充（在数据前面填充）
     */
    LEFT("左填充"),

    /**
     * 右填充（在数据后面填充）
     */
    RIGHT("右填充"),

    /**
     * 不填充
     */
    NONE("不填充");

    private final String description;

    PaddingDirection(String description) {
        this.description = description;
    }

    /**
     * 获取填充方向描述
     *
     * @return 填充方向描述
     */
    public String getDescription() {
        return description;
    }
}
