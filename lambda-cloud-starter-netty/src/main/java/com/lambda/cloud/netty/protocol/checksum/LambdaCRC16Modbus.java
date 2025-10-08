package com.lambda.cloud.netty.protocol.checksum;

import cn.hutool.core.io.checksum.crc16.CRC16Modbus;

public class LambdaCRC16Modbus extends CRC16Modbus {
    private final boolean reversed;

    public LambdaCRC16Modbus() {
        reversed = false;
    }

    public LambdaCRC16Modbus(boolean reversed) {
        this.reversed = reversed;
    }

    @Override
    public long getValue() {
        if(reversed){
            long crc = super.getValue();
            return ((crc & 0xFF) << 8) | ((crc >> 8) & 0xFF);
        }
        return super.getValue();
    }

}
