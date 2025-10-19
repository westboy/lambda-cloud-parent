package com.lambda.cloud.netty.protocol.converter.impl;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.converter.DataTypeConverter;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * CP56TIME2A时间格式转换器（增强版）
 * <p>
 * 实现IEC 60870-5协议中的7字节时间格式转换
 * 支持标志位设置、固定年份范围、Instant类型返回
 * </p>
 */
@Slf4j
public class CP56Time2aConverter implements DataTypeConverter {

    private static final int EXPECTED_LENGTH = 7;
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public Object parse(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        validateLength(data, fieldMetadata);

        try {
            LocalDateTime dateTime = getLocalDateTime(data, fieldMetadata);

            Class<?> fieldType = fieldMetadata.getFieldType();
            if (fieldType == String.class) {
                return dateTime.format(DEFAULT_FORMATTER);
            } else if (fieldType == LocalDateTime.class) {
                return dateTime;
            } else if (fieldType == Long.class || fieldType == long.class) {
                return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } else if (fieldType == Instant.class) {
                return dateTime.atZone(ZoneId.systemDefault()).toInstant();
            } else if (fieldType == byte[].class) {
                return data;
            } else {
                return dateTime.format(DEFAULT_FORMATTER);
            }
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "解析CP56TIME2A时间数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    private @NonNull LocalDateTime getLocalDateTime(byte[] data, ProtocolFieldMetadata fieldMetadata)
            throws ProtocolException {
        int milliseconds = ((data[1] & 0xFF) << 8) | (data[0] & 0xFF);
        if (milliseconds >= 60000) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "CP56TIME2A 毫秒字段无效: " + milliseconds,
                    fieldMetadata.getFieldName());
        }

        // 分钟和无效标志
        boolean invalid = (data[2] & 0x80) != 0;
        int minutes = data[2] & 0x3F;

        // 小时和夏令时标志
        boolean summerTime = (data[3] & 0x80) != 0;
        int hours = data[3] & 0x1F;

        // 日期和星期
        int originalDayOfWeek = (data[4] & 0xE0) >> 5;
        if (originalDayOfWeek == 0) originalDayOfWeek = 7;
        int dayOfMonth = data[4] & 0x1F;

        // 月份
        int month = data[5] & 0x0F;

        // 年份 (固定2000-2099)
        int year = 2000 + (data[6] & 0x7F);

        int seconds = milliseconds / 1000;
        int millis = milliseconds % 1000;

        if (invalid || !isValidDateTime(year, month, dayOfMonth, hours, minutes, seconds)) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR, "CP56TIME2A时间数据无效或标记为无效", fieldMetadata.getFieldName());
        }

        LocalDateTime dateTime = LocalDateTime.of(year, month, dayOfMonth, hours, minutes, seconds, millis * 1_000_000);

        int actualDayOfWeek = dateTime.getDayOfWeek().getValue();
        if (actualDayOfWeek != originalDayOfWeek) {
            log.warn("CP56TIME2A原始数据中的星期({})与实际日期计算的星期({})不符", originalDayOfWeek, actualDayOfWeek);
        }
        return dateTime;
    }

    @Override
    public byte[] serialize(Object value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        return serialize(value, fieldMetadata, false, false);
    }

    /**
     * 序列化带可选标志位（无效 / 夏令时）
     */
    public byte[] serialize(Object value, ProtocolFieldMetadata fieldMetadata, boolean invalid, boolean summerTime)
            throws ProtocolException {
        try {
            LocalDateTime dateTime = convertToLocalDateTime(value);
            if (dateTime == null) {
                throw new ProtocolException(
                        ProtocolException.ErrorCode.SERIALIZE_ERROR,
                        "不支持的时间数据类型: " + value.getClass().getName(),
                        fieldMetadata.getFieldName());
            }

            byte[] result = new byte[fieldMetadata.getLength()];

            int totalMilliseconds = dateTime.getSecond() * 1000 + dateTime.getNano() / 1_000_000;

            // 毫秒
            result[0] = (byte) (totalMilliseconds & 0xFF);
            result[1] = (byte) ((totalMilliseconds >> 8) & 0xFF);

            // 分钟 + 无效标志
            result[2] = (byte) (dateTime.getMinute() & 0x3F);
            if (invalid) result[2] |= (byte) 0x80;

            // 小时 + 夏令时
            result[3] = (byte) (dateTime.getHour() & 0x1F);
            if (summerTime) result[3] |= (byte) 0x80;

            // 日期和星期
            // int dayOfWeek = dateTime.getDayOfWeek().getValue();
            // result[4] = (byte) (((dayOfWeek & 0x07) << 5) | (dateTime.getDayOfMonth() & 0x1F));
            result[4] = (byte) ((0) | (dateTime.getDayOfMonth() & 0x1F));

            // 月份
            result[5] = (byte) (dateTime.getMonthValue() & 0x0F);

            // 年份
            int year = dateTime.getYear() % 100;
            result[6] = (byte) (year & 0x7F);

            return result;

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "序列化CP56TIME2A时间数据失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public Object parseFromString(String value, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        try {
            LocalDateTime dateTime = tryParseMultipleFormats(value);
            Class<?> fieldType = fieldMetadata.getFieldType();

            if (fieldType == LocalDateTime.class) return dateTime;
            if (fieldType == Long.class || fieldType == long.class)
                return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            if (fieldType == Instant.class)
                return dateTime.atZone(ZoneId.systemDefault()).toInstant();
            return dateTime.format(DEFAULT_FORMATTER);
        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "从字符串解析CP56TIME2A时间失败: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public void validateLength(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (data.length != EXPECTED_LENGTH) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "CP56TIME2A数据长度不匹配，期望: " + EXPECTED_LENGTH + ", 实际: " + data.length,
                    fieldMetadata.getFieldName());
        }
    }

    @Override
    public int getExpectedLength(ProtocolFieldMetadata fieldMetadata) {
        return EXPECTED_LENGTH;
    }

    private LocalDateTime convertToLocalDateTime(Object value) throws ProtocolException {
        return switch (value) {
            case LocalDateTime ldt -> ldt;
            case String str -> tryParseMultipleFormats(str);
            case Long timestamp -> LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            case Instant instant -> LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            case Timestamp ts -> ts.toLocalDateTime();
            case Date date -> date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            default -> null;
        };
    }

    private LocalDateTime tryParseMultipleFormats(String value) throws ProtocolException {
        DateTimeFormatter[] formats = new DateTimeFormatter[] {
            DEFAULT_FORMATTER,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
        };
        for (DateTimeFormatter fmt : formats) {
            try {
                return LocalDateTime.parse(value, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new ProtocolException(ProtocolException.ErrorCode.PARSE_ERROR, "无法解析时间字符串: " + value);
    }

    private boolean isValidDateTime(int year, int month, int day, int hour, int minute, int second) {
        try {
            if (year < 2000 || year > 2099) return false;
            if (month < 1 || month > 12) return false;
            if (day < 1 || day > 31) return false;
            if (hour < 0 || hour > 23) return false;
            if (minute < 0 || minute > 59) return false;
            return second >= 0 && second <= 59;
        } catch (Exception e) {
            return false;
        }
    }
}
