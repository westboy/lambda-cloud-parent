package com.lambda.cloud.netty.protocol.converter.impl;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.utils.ValidationUtils;
import io.netty.buffer.ByteBuf;

public class DataOffsetConverter implements DataTypeConverter {

    private static final int DEFAULT_OFFSET_VALUE = 0x33;

    @Override
    public Object parse(ByteBuf buffer, int length, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        byte[] data = new byte[length];
        buffer.readBytes(data);

        ValidationUtils.validateBasicInputs(data, fieldMetadata, "字节偏移");

        int offsetValue = getOffsetValue(fieldMetadata);

        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (data[i] - offsetValue);
        }

        return result;
    }

    @Override
    public void serialize(Object value, ByteBuf buffer, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        ValidationUtils.validateSerializeValue(value, fieldMetadata, "字节偏移");

        int offsetValue = getOffsetValue(fieldMetadata);

        byte[] data =
                switch (value) {
                    case byte[] bytes -> bytes;
                    case String str -> {
                        String hexStr = str.replaceAll("\\s+", "").replaceAll("^0x", "");
                        yield HexUtil.decodeHex(hexStr);
                    }
                    default ->
                        throw new ProtocolException(
                                ProtocolException.ErrorCode.SERIALIZE_ERROR,
                                "不支持的字节偏移数据类型: " + value.getClass().getName(),
                                fieldMetadata.getFieldName());
                };

        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] + offsetValue);
        }

        buffer.writeBytes(result);
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            String trimmed = value.trim();
            if (trimmed.startsWith("0x") || trimmed.startsWith("0X")) {
                trimmed = trimmed.substring(2);
            }
            String hexString = trimmed.replaceAll("\\s+", "");

            return HexUtil.decodeHex(hexString);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "从字符串解析字节偏移数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    private int getOffsetValue(ProtocolFieldMetadata fieldMetadata) {
        if (fieldMetadata.extParam() != null && fieldMetadata.extParam().containsKey("offsetValue")) {
            Object offsetObj = fieldMetadata.extParam().get("offsetValue");
            if (offsetObj instanceof Integer offsetInt) {
                return offsetInt;
            }
            if (offsetObj instanceof Number offsetNum) {
                return offsetNum.intValue();
            }
        }
        return DEFAULT_OFFSET_VALUE;
    }
}
