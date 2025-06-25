package com.lambda.cloud.iotdb.handler;

import org.apache.tsfile.read.common.RowRecord;

/**
 * MessageHandler
 *
 * @author Jin
 */
@FunctionalInterface
public interface MessageHandler {
    void handle(RowRecord record);
}
