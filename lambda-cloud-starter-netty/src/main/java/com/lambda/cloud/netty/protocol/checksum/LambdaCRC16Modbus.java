package com.lambda.cloud.netty.protocol.checksum;

import cn.hutool.core.io.checksum.crc16.CRC16Modbus;
import lombok.Getter;

/**
 * Lambda CRC16 Modbus 校验实现
 * <p>
 * 提供以下功能：
 * <ul>
 *   <li>标准 Modbus CRC16 校验</li>
 *   <li>支持字节序反转</li>
 *   <li>支持多种数据格式</li>
 *   <li>提供校验结果验证</li>
 * </ul>
 * </p>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 */
@Getter
public class LambdaCRC16Modbus extends CRC16Modbus {
    
    /**
     * 字节序反转标志
     */
    private final boolean reversed;

    /**
     * 校验和的字节数组
     */
    private byte[] checksumBytes;

    /**
     * 默认构造函数
     */
    public LambdaCRC16Modbus() {
        this(false);
    }

    /**
     * 指定字节序的构造函数
     *
     * @param reversed 是否反转字节序
     */
    public LambdaCRC16Modbus(boolean reversed) {
        this.reversed = reversed;
    }

    @Override
    public void update(byte[] b, int off, int len) {
        super.update(b, off, len);
        updateChecksumBytes();
    }

    @Override
    public void update(int b) {
        super.update(b);
        updateChecksumBytes();
    }

    @Override
    public long getValue() {
        if (reversed) {
            long crc = super.getValue();
            return ((crc & 0xFF) << 8) | ((crc >>> 8) & 0xFF);
        }
        return super.getValue();
    }

    /**
     * 获取校验和的十六进制字符串
     *
     * @return 十六进制字符串
     */
    public String getHexValue() {
        return String.format("%04X", getValue());
    }

    /**
     * 验证数据的校验和是否正确
     *
     * @param data 要验证的数据
     * @param checksum 预期的校验和
     * @return true 如果校验和匹配
     */
    public boolean verify(byte[] data, long checksum) {
        reset();
        update(data);
        return getValue() == checksum;
    }

    /**
     * 验证数据的校验和是否正确（十六进制字符串格式）
     *
     * @param data 要验证的数据
     * @param hexChecksum 预期的十六进制校验和
     * @return true 如果校验和匹配
     */
    public boolean verifyHex(byte[] data, String hexChecksum) {
        try {
            long checksum = Long.parseLong(hexChecksum, 16);
            return verify(data, checksum);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 获取校验和的字节数组
     *
     * @return 校验和的字节数组
     */
    public byte[] getChecksumBytes() {
        return checksumBytes.clone();
    }

    private void updateChecksumBytes() {
        long value = getValue();
        checksumBytes = new byte[]{
            (byte) ((value >>> 8) & 0xFF),
            (byte) (value & 0xFF)
        };
    }

    @Override
    public String toString() {
        return String.format(
            "LambdaCRC16Modbus{value=%d, hexValue='%s', reversed=%b}",
            getValue(), getHexValue(), reversed
        );
    }
}
