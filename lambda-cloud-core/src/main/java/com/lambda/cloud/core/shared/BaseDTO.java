package com.lambda.cloud.core.shared;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lambda.cloud.core.convert.BaseConverter;
import com.lambda.cloud.core.resolver.ConverterResolver;
import lombok.Data;

/**
 * DTO 基类
 *
 * @param <E>
 * @author Jin
 */
@Data
public abstract class BaseDTO<E> {

    /**
     * 转换为实体
     *
     * @return 实体
     */
    public E toEntity() {
        BaseConverter<BaseDTO<E>, E> converter = ConverterResolver.getConverter(this.getClass());
        return converter.convertTo(this);
    }

    /**
     * 获取查询条件
     *
     * @return 查询条件
     */
    @JsonIgnore
    public LambdaQueryWrapper<E> getLambdaQueryWrapper() {
        return Wrappers.lambdaQuery();
    }
}
