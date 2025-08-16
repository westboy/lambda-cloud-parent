package com.lambda.cloud.core.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.lambda.cloud.core.convert.BaseConverter;
import java.util.Map;
import org.mapstruct.Named;

import static com.lambda.cloud.core.Constants.GSON;

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

    @Named("mapToString")
    protected String mapToString(Map<String, Object> map) {
        if (map == null) return null;
        return GSON.toJson(map);
    }
}
