package com.lambda.cloud.netty.protocol.checksum.impl;

import com.lambda.cloud.netty.protocol.checksum.CrcAlgorithm;

public record Sum8Algorithm() implements CrcAlgorithm {

    @Override
    public long calculate(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }
        int sum = 0;
        for (byte b : data) {
            sum += (b & 0xFF);
        }
        return sum & 0xFF;
    }

    @Override
    public String algorithmName() {
        return "SUM8";
    }

    @Override
    public int getChecksumLength() {
        return 1;
    }
}
