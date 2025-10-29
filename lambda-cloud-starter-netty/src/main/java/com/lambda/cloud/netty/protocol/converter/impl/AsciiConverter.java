package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.annotation.PaddingDirection;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.utils.ValidationUtils;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * ASCII 字符串转换器
 * <p>
 * 提供ASCII字符串的解析和序列化功能
 * </p>
 *
 * @author Jin
 */
public class AsciiConverter implements DataTypeConverter {

    @Override
    public Object parse(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (ValidationUtils.isAllZero(data)) {
            return "";
        }

        // 验证输入参数
        ValidationUtils.validateBasicInputs(data, fieldMetadata, "ASCII");

        // 验证数据长度
        validateLength(data, fieldMetadata);

        try {
            Charset charset = getCharset(fieldMetadata);
            String result = new String(data, charset);

            // 移除填充字符
            char paddingChar = getPaddingChar(fieldMetadata);
            if (paddingChar != '\0') {
                result = removePadding(result, paddingChar, fieldMetadata);
            }

            return result;
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "解析ASCII数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public byte[] serialize(Object value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null) {
            value = "";
        }

        try {
            String stringValue = value.toString();
            Charset charset = getCharset(fieldMetadata);
            byte[] data = stringValue.getBytes(charset);

            // 调整长度
            return adjustLength(data, fieldMetadata.getLength(), fieldMetadata);

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化ASCII数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (value == null) {
            return "";
        }
        return value;
    }

    /**
     * 验证数据长度
     *
     * @param data          数据
     * @param fieldMetadata 字段元数据
     * @throws ProtocolException 验证失败时抛出
     */
    public void validateLength(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        int expectedLength = fieldMetadata.getLength();
        if (expectedLength != -1 && data.length != expectedLength) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "ASCII数据长度不匹配，期望: " + expectedLength + ", 实际: " + data.length,
                    fieldMetadata.getFieldName());
        }
    }

    /**
     * 获取字符集
     *
     * @param fieldMetadata 字段元数据
     * @return 字符集
     */
    private Charset getCharset(ProtocolFieldMetadata fieldMetadata) {
        String charsetName = fieldMetadata.getCharset();
        if (charsetName != null && !charsetName.isEmpty()) {
            try {
                return Charset.forName(charsetName);
            } catch (Exception e) {
                // 使用默认字符集
            }
        }
        return StandardCharsets.UTF_8;
    }

    /**
     * 获取填充字符
     *
     * @param fieldMetadata 字段元数据
     * @return 填充字符
     */
    private char getPaddingChar(ProtocolFieldMetadata fieldMetadata) {
        String paddingChar = fieldMetadata.getPaddingChar();
        if (paddingChar != null && !paddingChar.isEmpty()) {
            return paddingChar.charAt(0);
        }
        return '\0'; // 空字符
    }

    /**
     * 移除填充字符
     *
     * @param str           字符串
     * @param paddingChar   填充字符
     * @param fieldMetadata 字段元数据
     * @return 移除填充后的字符串
     */
    private String removePadding(String str, char paddingChar, ProtocolFieldMetadata fieldMetadata) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        // 根据填充方向移除填充字符
        PaddingDirection paddingDirection = fieldMetadata.getPaddingDirection();
        if (paddingDirection == PaddingDirection.LEFT) {
            // 左填充，从左边移除
            int start = 0;
            while (start < str.length() && str.charAt(start) == paddingChar) {
                start++;
            }
            return str.substring(start);
        } else {
            // 右填充（默认），从右边移除
            int end = str.length();
            while (end > 0 && str.charAt(end - 1) == paddingChar) {
                end--;
            }
            return str.substring(0, end);
        }
    }

    /**
     * 调整字节数组长度
     *
     * @param data          原始数据
     * @param targetLength  目标长度
     * @param fieldMetadata 字段元数据
     * @return 调整后的数据
     */
    private byte[] adjustLength(byte[] data, int targetLength, ProtocolFieldMetadata fieldMetadata) {
        if (data.length == targetLength) {
            return data;
        }

        byte[] result = new byte[targetLength];

        if (data.length < targetLength) {
            // 数据不足，填充
            char paddingChar = getPaddingChar(fieldMetadata);
            byte paddingByte = paddingChar == '\0' ? 0 : (byte) paddingChar;

            PaddingDirection paddingDirection = fieldMetadata.getPaddingDirection();
            if (paddingDirection == PaddingDirection.LEFT) {
                // 左填充
                Arrays.fill(result, 0, targetLength - data.length, paddingByte);
                System.arraycopy(data, 0, result, targetLength - data.length, data.length);
            } else {
                // 右填充（默认）
                System.arraycopy(data, 0, result, 0, data.length);
                Arrays.fill(result, data.length, targetLength, paddingByte);
            }
        } else {
            // 数据过长，截取
            System.arraycopy(data, 0, result, 0, targetLength);
        }

        return result;
    }
}
