package com.lambda.cloud.core.base;

import static com.lambda.cloud.core.Constants.GSON;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lambda.cloud.core.convert.BaseConverter;
import java.util.Map;
import org.mapstruct.Named;

/**
 * DTO 基类
 *
 * @param <D>
 * @param <E>
 * @author Jin
 */
public abstract class BaseDTO<D, E> {

    protected abstract Class<? extends BaseConverter<D, E>> getConverterClass();

    protected BaseConverter<D, E> getConverter() {
        Class<? extends BaseConverter<D, E>> converterClass = getConverterClass();
        return SpringUtil.getBean(converterClass);
    }

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
