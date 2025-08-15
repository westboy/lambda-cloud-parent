package com.lambda.cloud.core.convert;

import org.mapstruct.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通用转换器接口
 *
 * @param <S> 源对象类型
 * @param <T> 目标对象类型
 * @author Jin
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BaseConverter<S, T> {

    /**
     * 将源对象转换为目标对象
     *
     * @param source 源对象
     * @return 目标对象，如果输入为 null 则返回 null
     */
    @Mapping(target = "id", ignore = true)
    T convertTo(S source);

    /**
     * 将目标对象转换为源对象
     *
     * @param target 目标对象
     * @return 源对象，如果输入为 null 则返回 null
     */
    S convertFrom(T target);

    /**
     * 将源对象列表转换为目标对象列表
     *
     * @param sourceList 源对象列表
     * @return 目标对象列表，如果输入为 null 或空列表则返回空列表
     */
    List<T> convertToList(List<S> sourceList);

    /**
     * 将目标对象列表转换为源对象列表
     *
     * @param targetList 目标对象列表
     * @return 源对象列表，如果输入为 null 或空列表则返回空列表
     */
    List<S> convertFromList(List<T> targetList);

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
     * 将目标对象列表转换为指定类型的集合
     *
     * @param targetList 目标对象列表
     * @param <C>        集合类型
     * @return 源对象集合
     */
    @SuppressWarnings("unchecked")
    default <C extends Collection<S>> C convertFromCollection(List<T> targetList) {
        if (targetList == null || targetList.isEmpty()) {
            return (C) new ArrayList<S>();
        }
        return (C) convertFromList(targetList);
    }

    /**
     * 部分更新目标对象（仅更新非空字段）
     *
     * @param source 包含更新数据的源对象
     * @param target 目标对象
     * @return 更新后的目标对象
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    T updateTarget(S source, @MappingTarget T target);

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
     * 将目标对象转换为 Optional 包装的源对象
     *
     * @param target 目标对象
     * @return Optional 包装的源对象
     */
    default Optional<S> convertFromOptional(T target) {
        return Optional.ofNullable(convertFrom(target));
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
     * 过滤并转换非空的目标对象列表为源对象列表
     *
     * @param targetList 目标对象列表
     * @return 过滤后的源对象列表
     */
    default List<S> convertNonNullFromList(List<T> targetList) {
        if (targetList == null) {
            return new ArrayList<>();
        }
        return targetList.stream()
                .filter(Objects::nonNull)
                .map(this::convertFrom)
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

    /**
     * 批量转换并返回 Set 集合
     *
     * @param targetList 目标对象列表
     * @return 源对象 Set 集合
     */
    default Set<S> convertFromSet(List<T> targetList) {
        if (targetList == null || targetList.isEmpty()) {
            return new HashSet<>();
        }
        return targetList.stream()
                .filter(Objects::nonNull)
                .map(this::convertFrom)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}