package com.lambda.cloud.core.convert;

import static com.lambda.cloud.core.Constants.GSON;

import com.google.gson.reflect.TypeToken;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.mapstruct.Named;

/**
 * ConvertFunction
 *
 * @author Jin
 */
public interface ConvertFunction {

    // ------------------ Map 与 String ------------------

    /** Map -> String */
    @Named("mapToString")
    static String mapToString(Map<String, Object> map) {
        if (map == null) return null;
        return GSON.toJson(map);
    }

    /** String -> Map */
    @Named("stringToMap")
    static Map<String, Object> stringToMap(String json) {
        if (json == null || json.isEmpty()) return null;
        return GSON.fromJson(json, new TypeToken<>() {});
    }

    // ------------------ List 与 String ------------------

    /** List -> JSON String */
    @Named("listToString")
    static <T> String listToString(List<T> list) {
        if (list == null) return null;
        return GSON.toJson(list);
    }

    /** JSON String -> List */
    @Named("stringToList")
    static <T> List<T> stringToList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) return null;
        return GSON.fromJson(json, TypeToken.getParameterized(List.class, clazz).getType());
    }

    // ------------------ LocalDate / LocalDateTime ------------------

    /** LocalDateTime -> String */
    @Named("localDateTimeToString")
    static String localDateTimeToString(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /** String -> LocalDateTime */
    @Named("stringToLocalDateTime")
    static LocalDateTime stringToLocalDateTime(String str) {
        if (str == null || str.isEmpty()) return null;
        return LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /** LocalDate -> String */
    @Named("localDateToString")
    static String localDateToString(LocalDate date) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /** String -> LocalDate */
    @Named("stringToLocalDate")
    static LocalDate stringToLocalDate(String str) {
        if (str == null || str.isEmpty()) return null;
        return LocalDate.parse(str, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    // ------------------ Number 类型转换 ------------------

    @Named("longToString")
    static String longToString(Long value) {
        return value == null ? null : value.toString();
    }

    @Named("stringToLong")
    static Long stringToLong(String value) {
        return (value == null || value.isEmpty()) ? null : Long.parseLong(value);
    }

    @Named("integerToString")
    static String integerToString(Integer value) {
        return value == null ? null : value.toString();
    }

    @Named("stringToInteger")
    static Integer stringToInteger(String value) {
        return (value == null || value.isEmpty()) ? null : Integer.parseInt(value);
    }

    @Named("doubleToString")
    static String doubleToString(Double value) {
        return value == null ? null : value.toString();
    }

    @Named("stringToDouble")
    static Double stringToDouble(String value) {
        return (value == null || value.isEmpty()) ? null : Double.parseDouble(value);
    }

    // ------------------ 布尔类型 ------------------

    @Named("booleanToString")
    static String booleanToString(Boolean value) {
        return value == null ? null : value.toString();
    }

    @Named("stringToBoolean")
    static Boolean stringToBoolean(String value) {
        return (value == null || value.isEmpty()) ? null : Boolean.parseBoolean(value);
    }

    // ------------------ Enum 与 String ------------------

    @Named("enumToString")
    static <E extends Enum<E>> String enumToString(E e) {
        return e == null ? null : e.name();
    }

    @Named("stringToEnum")
    static <E extends Enum<E>> E stringToEnum(String name, Class<E> enumClass) {
        if (name == null || name.isEmpty()) return null;
        return Enum.valueOf(enumClass, name);
    }

    // ------------------ UUID ------------------

    @Named("uuidToString")
    static String uuidToString(UUID uuid) {
        return uuid == null ? null : uuid.toString();
    }

    @Named("stringToUUID")
    static UUID stringToUUID(String str) {
        if (str == null || str.isEmpty()) return null;
        return UUID.fromString(str);
    }
}
