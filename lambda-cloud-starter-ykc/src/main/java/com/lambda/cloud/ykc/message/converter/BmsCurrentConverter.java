package com.lambda.cloud.ykc.message.converter;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import io.netty.buffer.ByteBuf;

/**
 * BMS 电流专用解析器
 */
public class BmsCurrentConverter implements DataTypeConverter {

    private final double resolution; // 每位电流值
    private final double offset; // 偏移量

    public BmsCurrentConverter(double resolution, double offset) {
        this.resolution = resolution;
        this.offset = offset;
    }

    @Override
    public Object parse(ByteBuf buffer, int length, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (length != 2) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "BMS电流字段必须为2字节", fieldMetadata.getFieldName());
        }

        byte high = buffer.readByte();
        byte low = buffer.readByte();

        // 大端解析16位无符号整数
        int raw = ((high & 0xFF) << 8) | (low & 0xFF);

        // 转成带符号补码
        if (raw >= 0x8000) {
            raw -= 0x10000;
        }

        // 转成实际电流
        return raw * resolution + offset;
    }

    @Override
    public void serialize(Object value, ByteBuf buffer, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (!(value instanceof Number number)) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR, "BMS电流必须为数字类型", fieldMetadata.getFieldName());
        }

        // 转回原始整数
        int raw = (int) ((number.doubleValue() - offset) / resolution);

        // 检查范围
        if (raw < -32768 || raw > 32767) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "BMS电流值超出16位带符号范围: " + number,
                    fieldMetadata.getFieldName());
        }

        // 转成补码形式
        if (raw < 0) {
            raw += 0x10000;
        }

        buffer.writeByte((raw >> 8) & 0xFF);
        buffer.writeByte(raw & 0xFF);
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "BMS电流字符串格式错误: " + value, fieldMetadata.getFieldName());
        }
    }
}
