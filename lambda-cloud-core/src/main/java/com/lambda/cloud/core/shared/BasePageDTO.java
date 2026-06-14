package com.lambda.cloud.core.shared;

import com.lambda.cloud.core.Constants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 分页数据传输对象基类
 * <p>
 * 该抽象类为所有需要分页功能的DTO提供统一的分页参数和查询构造器。
 * 封装了常用的分页逻辑，简化分页查询的实现。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>提供统一的分页参数（页码、页大小）</li>
 *   <li>支持参数验证，确保分页参数有效</li>
 *   <li>提供MyBatis-Plus分页对象的创建</li>
 * </ul>
 *
 * @author Jin
 * @see com.baomidou.mybatisplus.extension.plugins.pagination.Page
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BasePageDTO implements PageRequest {

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大页大小限制
     */
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * 最小页大小
     */
    public static final int MIN_PAGE_SIZE = 1;

    /**
     * 页码
     * <p>
     * 当前查询的页码，从1开始计数。
     * 默认值为1，表示查询第一页数据。
     * </p>
     */
    @Schema(description = "当前页码，从1开始", example = "1", defaultValue = "1")
    @NotNull(message = Constants.MSG_PAGE_NUM_NOT_NULL)
    @Min(value = 1, message = "页码必须大于等于1")
    protected Integer pageNum = DEFAULT_PAGE_NUM;

    /**
     * 每页记录数
     * <p>
     * 每页显示的记录数量。默认值为Integer.MAX_VALUE，
     * 表示不限制每页记录数（相当于查询所有数据）。
     * 在实际使用中，建议设置合理的页大小以提高性能。
     * 默认20条，最大1000条，防止大数据量查询影响性能
     * </p>
     */
    @Schema(description = "每页条数", example = "20", defaultValue = "20")
    @NotNull(message = Constants.MSG_PAGE_SIZE_NOT_NULL)
    @Min(value = MIN_PAGE_SIZE, message = "每页条数必须大于等于1")
    @Max(value = MAX_PAGE_SIZE, message = "每页条数不能超过1000")
    protected Integer pageSize = DEFAULT_PAGE_SIZE;
}
