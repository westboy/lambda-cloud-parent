package com.lambda.cloud.core.shared;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

/**
 * 分页请求
 *
 */
public interface PageRequest extends Serializable {

    /**
     * 页码
     */
    Integer getPageSize();

    /**
     * 每页记录数
     */
    Integer getPageNum();

    /**
     * 创建分页对象
     * <p>
     * 根据当前的页码和页大小创建MyBatis-Plus的分页对象。
     * 该方法为分页查询提供便捷的工具。
     * </p>
     *
     * @return MyBatis-Plus分页对象
     * @see com.baomidou.mybatisplus.extension.plugins.pagination.Page
     */
    @JsonIgnore
    default <T> IPage<T> getPage() {
        return new Page<>(getPageNum(), getPageSize());
    }
}
