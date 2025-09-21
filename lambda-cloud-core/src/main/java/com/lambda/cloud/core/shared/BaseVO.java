package com.lambda.cloud.core.shared;

import java.util.List;

/**
 * VO 基类
 *
 * @param <V>
 * @param <E>
 * @author Jin
 */
public abstract class BaseVO<V, E> {

    private final ConvertCache<V, E> convertCache = new ConvertCache<>();

    /**
     * 转换为实体
     *
     * @param entity 实体
     * @return VO
     */
    public V fromEntity(E entity) {
        return convertCache.getConverter(this.getClass()).convertFrom(entity);
    }

    /**
     * 转换为实体列表
     *
     * @param entityList 实体列表
     * @return VO列表
     */
    public List<V> fromEntityList(List<E> entityList) {
        return convertCache.getConverter(this.getClass()).convertFromList(entityList);
    }
}
