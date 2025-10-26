package com.lambda.cloud.ykc.message;

public interface ProtocolMessage {
    String getFrameType();
    void setFrameType(String frameType);
    Integer getSerialNumber();
    void setSerialNumber(Integer serialNumber);
}
