package com.lambda.cloud.core.shared;

import com.lambda.cloud.core.convert.BaseConverter;
import com.lambda.cloud.core.resolver.ConverterResolver;
import java.util.List;

/**
 * VO 基类
 *
 * @param <E>
 * @author Jin
 */
public abstract class BaseVO<E> {

    /**
     * 转换为实体
     *
     * @param entity 实体
     * @return VO
     */
    public <V extends BaseVO<E>> V fromEntity(E entity) {
        BaseConverter<V, E> converter = ConverterResolver.getConverter(this.getClass());
        return converter.convertFrom(entity);
    }

    /**
     * 转换为实体列表
     *
     * @param entityList 实体列表
     * @return VO列表
     */
    public <V extends BaseVO<E>> List<V> fromEntityList(List<E> entityList) {
        BaseConverter<V, E> converter = ConverterResolver.getConverter(this.getClass());
        return converter.convertFromList(entityList);
    }
}
