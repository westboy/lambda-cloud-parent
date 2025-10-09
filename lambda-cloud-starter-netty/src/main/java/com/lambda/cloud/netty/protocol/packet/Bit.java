package com.lambda.cloud.netty.protocol.packet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 位字段数据单元，表示一个位字段
 *
 */
@Getter
@ToString
@EqualsAndHashCode
@Slf4j
public class Bit {
    /**
     * 最大支持的位长度（32位）
     */
    public static final int MAX_LENGTH = 32;

    /**
     * 在位容器中的起始位置
     */
    private int site;

    /**
     * 位字段的数据值
     */
    private int data;

    /**
     * 位字段的长度（位数）
     */
    private final int length;

    /**
     * 最大值缓存（性能优化）
     * -- GETTER --
     *  获取最大值
     *
     *
     */
    private final int maxValue;

    /**
     * 位掩码缓存（性能优化）
     */
    private final int mask;

    /**
     * 构造函数
     *
     * @param length 位字段长度，必须在1-32之间
     * @throws IllegalArgumentException 如果长度无效
     */
    public Bit(int length) {
        validateLength(length);
        this.length = length;
        this.maxValue = calculateMaxValue(length);
        this.mask = calculateMask(length);
        this.data = 0;
        this.site = 0;
    }

    /**
     * 构造函数，带初始数据
     *
     * @param length 位字段长度
     * @param data 初始数据值
     */
    public Bit(int length, int data) {
        this(length);
        setData(data);
    }

    /**
     * 构造函数，带初始数据和位置
     *
     * @param length 位字段长度
     * @param data 初始数据值
     * @param site 在位容器中的位置
     */
    public Bit(int length, int data, int site) {
        this(length, data);
        setSite(site);
    }

    /**
     * 验证位长度
     */
    private static void validateLength(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Bit length must be positive, got: " + length);
        }
        if (length > MAX_LENGTH) {
            throw new IllegalArgumentException("Bit length cannot exceed " + MAX_LENGTH + ", got: " + length);
        }
    }

    /**
     * 计算最大值（性能优化）
     */
    private static int calculateMaxValue(int length) {
        return length >= 31 ? Integer.MAX_VALUE : (1 << length) - 1;
    }

    /**
     * 计算位掩码（性能优化）
     */
    private static int calculateMask(int length) {
        return length >= 31 ? 0xFFFFFFFF : (1 << length) - 1;
    }

    /**
     * 获取用于打印的数据
     *
     * @return 数据的字符串表示
     */
    public String getPrintData() {
        return String.valueOf(this.data);
    }

    /**
     * 设置位字段的数据值（高性能版本）
     *
     * @param data 要设置的数据值
     * @throws IllegalArgumentException 如果数据为负数
     */
    public void setData(int data) {
        if (data < 0) {
            throw new IllegalArgumentException("Bit data cannot be negative, got: " + data);
        }

        // 使用位运算进行范围检查和截断（性能优化）
        this.data = data & mask;

        if (data > maxValue && log.isDebugEnabled()) {
            log.debug(
                    "Data {} exceeds max value {} for {}-bit field, truncated to {}",
                    data,
                    maxValue,
                    length,
                    this.data);
        }
    }

    /**
     * 设置位字段的数据值，支持溢出处理策略
     *
     * @param data 要设置的数据值
     * @param overflowStrategy 溢出处理策略
     */
    public void setData(int data, OverflowStrategy overflowStrategy) {
        if (data < 0) {
            throw new IllegalArgumentException("Bit data cannot be negative, got: " + data);
        }

        switch (overflowStrategy) {
            case TRUNCATE:
                this.data = data & mask;
                break;
            case SATURATE:
                this.data = Math.min(data, maxValue);
                break;
            case THROW:
                if (data > maxValue) {
                    throw new IllegalArgumentException(
                            String.format("Data %d exceeds max value %d for %d-bit field", data, maxValue, length));
                }
                this.data = data;
                break;
        }
    }

    /**
     * 设置在位容器中的位置
     *
     * @param site 起始位置
     * @throws IllegalArgumentException 如果位置为负数
     */
    public void setSite(int site) {
        if (site < 0) {
            throw new IllegalArgumentException("Bit site cannot be negative, got: " + site);
        }
        this.site = site;
    }

    /**
     * 获取位字段的二进制表示（高性能版本）
     *
     * @return 二进制字符串
     */
    public String getBinaryString() {
        if (length <= 0) {
            return "";
        }

        // 使用位运算构建二进制字符串（性能优化）
        StringBuilder sb = new StringBuilder(length);
        for (int i = length - 1; i >= 0; i--) {
            sb.append((data >>> i) & 1);
        }
        return sb.toString();
    }

    /**
     * 从二进制字符串设置数据（高性能版本）
     *
     * @param binaryString 二进制字符串
     * @throws IllegalArgumentException 如果二进制字符串无效
     */
    public void setFromBinaryString(String binaryString) {
        if (binaryString == null || binaryString.isEmpty()) {
            this.data = 0;
            return;
        }

        // 验证二进制字符串格式
        if (!binaryString.matches("[01]+")) {
            throw new IllegalArgumentException("Invalid binary string: " + binaryString);
        }

        // 截取或填充到指定长度
        this.data = getAnInt(binaryString);
    }

    private int getAnInt(String binaryString) {
        String processedString;
        if (binaryString.length() > length) {
            processedString = binaryString.substring(binaryString.length() - length);
        } else {
            // 使用位运算填充前导零（性能优化）
            processedString = String.format("%" + length + "s", binaryString).replace(' ', '0');
        }

        // 使用位运算解析二进制字符串（性能优化）
        int result = 0;
        for (int i = 0; i < processedString.length(); i++) {
            result = (result << 1) | (processedString.charAt(i) - '0');
        }
        return result;
    }

    /**
     * 设置指定位置的位为1
     *
     * @param position 位置（0为最低位）
     * @throws IllegalArgumentException 如果位置超出范围
     */
    public void setBit(int position) {
        validateBitPosition(position);
        this.data |= (1 << position);
    }

    /**
     * 清除指定位置的位（设置为0）
     *
     * @param position 位置（0为最低位）
     * @throws IllegalArgumentException 如果位置超出范围
     */
    public void clearBit(int position) {
        validateBitPosition(position);
        this.data &= ~(1 << position);
    }

    /**
     * 切换指定位置的位
     *
     * @param position 位置（0为最低位）
     * @throws IllegalArgumentException 如果位置超出范围
     */
    public void toggleBit(int position) {
        validateBitPosition(position);
        this.data ^= (1 << position);
    }

    /**
     * 测试指定位置的位是否为1
     *
     * @param position 位置（0为最低位）
     * @return 如果该位为1则返回true
     * @throws IllegalArgumentException 如果位置超出范围
     */
    public boolean testBit(int position) {
        validateBitPosition(position);
        return (this.data & (1 << position)) != 0;
    }

    /**
     * 验证位位置
     */
    private void validateBitPosition(int position) {
        if (position < 0 || position >= length) {
            throw new IllegalArgumentException(
                    String.format("Bit position %d is out of range [0, %d)", position, length));
        }
    }

    /**
     * 获取布尔值（仅适用于1位字段）
     *
     * @return 布尔值
     * @throws IllegalStateException 如果不是1位字段
     */
    public boolean getAsBoolean() {
        if (length != 1) {
            throw new IllegalStateException("getAsBoolean() only supported for 1-bit fields");
        }
        return data != 0;
    }

    /**
     * 设置布尔值（仅适用于1位字段）
     *
     * @param value 布尔值
     * @throws IllegalStateException 如果不是1位字段
     */
    public void setAsBoolean(boolean value) {
        if (length != 1) {
            throw new IllegalStateException("setAsBoolean() only supported for 1-bit fields");
        }
        this.data = value ? 1 : 0;
    }

    /**
     * 获取字节值（仅适用于8位以下字段）
     *
     * @return 字节值
     * @throws IllegalStateException 如果超过8位
     */
    public byte getAsByte() {
        if (length > 8) {
            throw new IllegalStateException("getAsByte() only supported for fields <= 8 bits");
        }
        return (byte) data;
    }

    /**
     * 设置字节值（仅适用于8位以下字段）
     *
     * @param value 字节值
     * @throws IllegalStateException 如果超过8位
     */
    public void setAsByte(byte value) {
        if (length > 8) {
            throw new IllegalStateException("setAsByte() only supported for fields <= 8 bits");
        }
        setData(value & 0xFF);
    }

    /**
     * 检查是否为空（所有位都为0）
     *
     * @return 如果所有位都为0则返回true
     */
    public boolean isEmpty() {
        return data == 0;
    }

    /**
     * 检查是否已满（所有位都为1）
     *
     * @return 如果所有位都为1则返回true
     */
    public boolean isFull() {
        return data == maxValue;
    }

    /**
     * 清空所有位（设置为0）
     */
    public void clear() {
        this.data = 0;
    }

    /**
     * 填满所有位（设置为1）
     */
    public void fill() {
        this.data = maxValue;
    }

    /**
     * 获取设置的位数量
     *
     * @return 设置为1的位数量
     */
    public int getSetBitCount() {
        return Integer.bitCount(data);
    }

    /**
     * 创建该位字段的副本
     *
     * @return 新的Bit实例
     */
    public Bit copy() {
        return new Bit(length, data, site);
    }

    /**
     * 构建器模式
     */
    public static class Builder {
        private int length;
        private int data = 0;
        private int site = 0;

        public Builder length(int length) {
            this.length = length;
            return this;
        }

        public Builder data(int data) {
            this.data = data;
            return this;
        }

        public Builder site(int site) {
            this.site = site;
            return this;
        }

        public Builder fromBoolean(boolean value) {
            this.length = 1;
            this.data = value ? 1 : 0;
            return this;
        }

        public Builder fromByte(byte value) {
            this.length = 8;
            this.data = value & 0xFF;
            return this;
        }

        public Bit build() {
            return new Bit(length, data, site);
        }
    }

    /**
     * 创建构建器
     *
     * @return 新的构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 创建1位布尔字段
     *
     * @param value 布尔值
     * @return 新的Bit实例
     */
    public static Bit ofBoolean(boolean value) {
        return new Bit(1, value ? 1 : 0);
    }

    /**
     * 创建8位字节字段
     *
     * @param value 字节值
     * @return 新的Bit实例
     */
    public static Bit ofByte(byte value) {
        return new Bit(8, value & 0xFF);
    }
}
