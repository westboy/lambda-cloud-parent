package com.jingfang.cloud.kafka;

/**
 * Factory
 *
 * @author jin
 */
public final class Factory {
    private Factory() {
    }

    public static final String STRING = "stringContainerFactory";
    public static final String JSON = "jsonContainerFactory";
    public static final String OBJECT = "objectContainerFactory";
    public static final String BATCH = "batchObjectContainerFactory";
}
