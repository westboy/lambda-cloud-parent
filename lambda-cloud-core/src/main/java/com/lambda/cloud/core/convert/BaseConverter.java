package com.lambda.cloud.core.convert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通用转换器接口
 *
 * @param <S> 源对象类型
 * @param <T> 目标对象类型
 * @author Jin
 */
public interface BaseConverter<S, T> {

    /**
     * 将源对象转换为目标对象
     *
     * @param source 源对象
     * @return 目标对象，如果输入为 null 则返回 null
     */
    T convertTo(S source);

    /**
     * 将源对象列表转换为目标对象列表
     *
     * @param sourceList 源对象列表
     * @return 目标对象列表，如果输入为 null 或空列表则返回空列表
     */
    List<T> convertToList(List<S> sourceList);

    /**
     * 将源对象列表转换为指定类型的集合
     *
     * @param sourceList 源对象列表
     * @param <C>        集合类型
     * @return 目标对象集合
     */
    @SuppressWarnings("unchecked")
    default <C extends Collection<T>> C convertToCollection(List<S> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) {
            return (C) new ArrayList<T>();
        }
        return (C) convertToList(sourceList);
    }

    /**
     * 将源对象转换为 Optional 包装的目标对象
     *
     * @param source 源对象
     * @return Optional 包装的目标对象
     */
    default Optional<T> convertToOptional(S source) {
        return Optional.ofNullable(convertTo(source));
    }

    /**
     * 过滤并转换非空的源对象列表为目标对象列表
     *
     * @param sourceList 源对象列表
     * @return 过滤后的目标对象列表
     */
    default List<T> convertNonNullToList(List<S> sourceList) {
        if (sourceList == null) {
            return new ArrayList<>();
        }
        return sourceList.stream()
                .filter(Objects::nonNull)
                .map(this::convertTo)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换并返回 Set 集合
     *
     * @param sourceList 源对象列表
     * @return 目标对象 Set 集合
     */
    default Set<T> convertToSet(List<S> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new HashSet<>();
        }
        return sourceList.stream()
                .filter(Objects::nonNull)
                .map(this::convertTo)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
