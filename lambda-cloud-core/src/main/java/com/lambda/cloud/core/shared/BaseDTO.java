package com.lambda.cloud.core.shared;

import static com.lambda.cloud.core.Constants.GSON;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private final ConvertCache<D, E> convertCache = new ConvertCache<>();

    /**
     * 转换为实体
     *
     * @return 实体
     */
    @SuppressWarnings("unchecked")
    public E toEntity() {
        return convertCache.getConverter(this.getClass()).convertTo((D) this);
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

    /**
     * Map 转 String
     *
     * @param map Map<String, Object>
     * @return String
     */
    @Named("mapToString")
    protected String mapToString(Map<String, Object> map) {
        if (map == null) return null;
        return GSON.toJson(map);
    }
}
