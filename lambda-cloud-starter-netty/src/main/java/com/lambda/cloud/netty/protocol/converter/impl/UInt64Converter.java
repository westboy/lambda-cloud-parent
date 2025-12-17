package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.utils.PrimitiveTypeUtils;
import io.netty.buffer.ByteBuf;
import java.math.BigInteger;

/**
 * 64位无符号整数转换器（8字节）
 * <p>
 * 解析/序列化无符号64位整数，支持大小端字节序。
 * 对于超过 Long.MAX_VALUE 的值，优先返回字符串表示或 BigInteger（取决于字段类型）。
 * </p>
 */
public class UInt64Converter implements DataTypeConverter {

    private static final BigInteger MAX_UINT64 = new BigInteger("FFFFFFFFFFFFFFFF", 16);
    private static final BigInteger MIN_UINT64 = BigInteger.ZERO;

    @Override
    public Object parse(ByteBuf buffer, int length, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        byte[] data = new byte[length];
        buffer.readBytes(data);
        if (data.length != 8) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "UINT64数据长度必须为8字节，实际: " + data.length,
                    fieldMetadata.getFieldName());
        }

        try {
            byte[] beBytes;
            if (fieldMetadata.isLittleEndian()) {
                beBytes = reverse(data);
            } else {
                beBytes = data.clone();
            }

            BigInteger value = new BigInteger(1, beBytes); // 正数

            return convertTo(fieldMetadata, value);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "解析UINT64数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public void serialize(Object value, ByteBuf buffer, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null) {
            buffer.writeBytes(new byte[8]);
            return;
        }

        try {
            BigInteger bigValue;
            switch (value) {
                case BigInteger bigInteger -> bigValue = bigInteger;
                case Number number -> {
                    // 将 Number 转为无符号表示（负数非法）
                    long lv = number.longValue();
                    if (lv < 0) {
                        throw new ProtocolException(
                                ProtocolException.ErrorCode.SERIALIZE_ERROR,
                                "UINT64不支持负数: " + lv,
                                fieldMetadata.getFieldName());
                    }
                    bigValue = BigInteger.valueOf(lv);
                }
                case String string -> {
                    String s = string.trim();
                    bigValue = new BigInteger(s);
                }
                default ->
                    throw new ProtocolException(
                            ProtocolException.ErrorCode.SERIALIZE_ERROR,
                            "不支持的UINT64数据类型: " + value.getClass().getName(),
                            fieldMetadata.getFieldName());
            }

            // 范围校验：0 <= v <= 2^64-1
            if (bigValue.compareTo(MIN_UINT64) < 0 || bigValue.compareTo(MAX_UINT64) > 0) {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.SERIALIZE_ERROR,
                        "UINT64数值超出范围: " + bigValue,
                        fieldMetadata.getFieldName());
            }

            byte[] be = toFixedLengthBytes(bigValue);
            if (fieldMetadata.isLittleEndian()) {
                buffer.writeBytes(reverse(be));
            } else {
                buffer.writeBytes(be);
            }
        } catch (ProtocolException e) {
            throw e;
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化UINT64数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null || value.trim().isEmpty()) {
            return BigInteger.ZERO;
        }
        try {
            BigInteger v = new BigInteger(value.trim());
            if (v.compareTo(MIN_UINT64) < 0 || v.compareTo(MAX_UINT64) > 0) {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.PARSE_ERROR, "UINT64字符串超出范围: " + v, fieldMetadata.getFieldName());
            }
            return convertTo(fieldMetadata, v);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "从字符串解析UINT64失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    private Object convertTo(ProtocolFieldMetadata fieldMetadata, BigInteger v) {
        Class<?> fieldType = fieldMetadata.getFieldType();
        if (PrimitiveTypeUtils.isLongType(fieldType)) {
            if (v.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
                return v.longValue();
            } else {
                return v.toString();
            }
        } else if (PrimitiveTypeUtils.isIntegerType(fieldType)) {
            if (v.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) <= 0) {
                return v.intValue();
            } else {
                return v.toString();
            }
        } else if (fieldType == String.class) {
            return v.toString();
        } else if (fieldType == BigInteger.class) {
            return v;
        } else {
            return v.toString();
        }
    }

    @Override
    public int getExpectedLength(ProtocolFieldMetadata fieldMetadata) {
        return 8;
    }

    private static byte[] reverse(byte[] data) {
        byte[] r = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            r[i] = data[data.length - 1 - i];
        }
        return r;
    }

    private static byte[] toFixedLengthBytes(BigInteger value) {
        byte[] bytes = value.toByteArray(); // big-endian with sign
        if (bytes.length == 8) {
            return bytes;
        } else if (bytes.length < 8) {
            byte[] padded = new byte[8];
            System.arraycopy(bytes, 0, padded, 8 - bytes.length, bytes.length);
            return padded;
        } else {
            // 超长则需要检查是否为前导0扩展
            int offset = bytes.length - 8;
            // 若高位都是0并仅长度超出，可截断前导0
            boolean allLeadingZero = true;
            for (int i = 0; i < offset; i++) {
                if (bytes[i] != 0) {
                    allLeadingZero = false;
                    break;
                }
            }
            if (allLeadingZero) {
                byte[] truncated = new byte[8];
                System.arraycopy(bytes, offset, truncated, 0, 8);
                return truncated;
            }
            throw new IllegalArgumentException("UINT64 超出固定长度: " + value);
        }
    }
}
