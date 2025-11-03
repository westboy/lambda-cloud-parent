package com.lambda.cloud.netty.protocol.message;

public interface ProtocolMessage {

    String getFrameType();

    void setFrameType(String frameType);

    Integer getSerialNumber();

    void setSerialNumber(Integer serialNumber);

    default String getTerminalId() {
        throw new UnsupportedOperationException("not implement");
    }
}
