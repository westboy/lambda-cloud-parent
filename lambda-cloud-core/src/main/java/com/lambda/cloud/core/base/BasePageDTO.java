package com.lambda.cloud.core.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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
 *   <li>提供Lambda查询构造器的创建</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * public class UserPageDTO extends BasePageDTO<User> {
 *     private String username;
 *     private Integer status;
 *
 *     public LambdaQueryWrapper<User> buildQueryWrapper() {
 *         LambdaQueryWrapper<User> wrapper = getLambdaQueryWrapper();
 *         wrapper.like(StringUtils.hasText(username), User::getUsername, username)
 *                .eq(status != null, User::getStatus, status);
 *         return wrapper;
 *     }
 * }
 * }</pre>
 *
 * @param <T> 实体类型
 * @author Jin
 * @see com.baomidou.mybatisplus.extension.plugins.pagination.Page
 * @see com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
 */
@Getter
@Setter
public abstract class BasePageDTO<T> {
    /**
     * 页码
     * <p>
     * 当前查询的页码，从1开始计数。
     * 默认值为1，表示查询第一页数据。
     * </p>
     */
    @NotNull(message = "pageNum不能为空")
    private Integer pageNum = 1;

    /**
     * 每页记录数
     * <p>
     * 每页显示的记录数量。默认值为Integer.MAX_VALUE，
     * 表示不限制每页记录数（相当于查询所有数据）。
     * 在实际使用中，建议设置合理的页大小以提高性能。
     * </p>
     */
    @NotNull(message = "pageSize不能为空")
    private Integer pageSize = Integer.MAX_VALUE;

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
    public Page<T> getPage() {
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 创建Lambda查询构造器
     * <p>
     * 创建一个新的Lambda查询构造器，用于构建类型安全的查询条件。
     * 子类可以基于此构造器添加具体的查询条件。
     * </p>
     *
     * <h3>使用建议：</h3>
     * <ul>
     *   <li>在子类中重写此方法或创建新的查询构造方法</li>
     *   <li>使用Lambda表达式避免硬编码字段名</li>
     *   <li>合理使用条件判断，避免无效查询条件</li>
     * </ul>
     *
     * @return Lambda查询构造器
     * @see com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
     */
    protected LambdaQueryWrapper<T> getLambdaQueryWrapper() {
        return Wrappers.lambdaQuery();
    }
}
