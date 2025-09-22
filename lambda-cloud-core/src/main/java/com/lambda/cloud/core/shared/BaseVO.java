package com.lambda.cloud.core.shared;

import java.util.List;

/**
 * VO 基类
 *
 * @param <E>
 * @author Jin
 */
public abstract class BaseVO<E> {

    private final ConverterResolver converterResolver = new ConverterResolver();

    /**
     * 转换为实体
     *
     * @param entity 实体
     * @return VO
     */
    public <V extends BaseVO<E>> V fromEntity(E entity) {
        BaseConverter<V, E> converter = converterResolver.getConverter(this.getClass());
        return converter.convertFrom(entity);
    }

    /**
     * 转换为实体列表
     *
     * @param entityList 实体列表
     * @return VO列表
     */
    public <V extends BaseVO<E>> List<V> fromEntityList(List<E> entityList) {
        BaseConverter<V, E> converter = converterResolver.getConverter(this.getClass());
        return converter.convertFromList(entityList);
    }
}
