package com.lambda.cloud.core.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lambda.cloud.core.convert.BaseConverter;

/**
 * DTO 基类
 *
 * @param <D>
 * @param <E>
 * @author Jin
 */
public abstract class BaseDTO<D, E> {

    protected abstract BaseConverter<D, E> getConverter();

    @SuppressWarnings("unchecked")
    public E toEntity() {
        return getConverter().convertTo((D) this);
    }

    @JsonIgnore
    public LambdaQueryWrapper<E> getLambdaQueryWrapper() {
        return Wrappers.lambdaQuery();
    }
}
