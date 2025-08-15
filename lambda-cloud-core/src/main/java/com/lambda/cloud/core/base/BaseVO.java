package com.lambda.cloud.core.base;

import com.lambda.cloud.core.convert.BaseConverter;

import java.util.List;

/**
 * VO 基类
 *
 * @param <V>
 * @param <E>
 * @author Jin
 */
public abstract class BaseVO<V, E> {

    protected abstract BaseConverter<V, E> getConverter();

    /**
     * 转换为实体
     * @param entity 实体
     * @return VO
     */
    public V fromEntity(E entity) {
        return getConverter().convertFrom(entity);
    }

    /**
     * 转换为实体列表
     * @param entityList 实体列表
     * @return VO列表
     */
    public List<V> fromEntityList(List<E> entityList) {
        return getConverter().convertFromList(entityList);
    }
}
