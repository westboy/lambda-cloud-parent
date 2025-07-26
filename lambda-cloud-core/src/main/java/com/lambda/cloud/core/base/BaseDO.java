package com.lambda.cloud.core.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据库实体基类（Data Object）
 * <p>
 * 该抽象类为所有数据库实体提供通用的基础字段，包括创建信息、更新信息和逻辑删除标识。
 * 使用MyBatis-Plus的注解实现字段的自动填充和逻辑删除功能。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>提供统一的审计字段（创建人、创建时间、更新人、更新时间）</li>
 *   <li>支持逻辑删除，避免物理删除数据</li>
 *   <li>实现序列化接口，支持对象序列化</li>
 *   <li>使用Lombok简化getter/setter方法</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * @Entity
 * @Table(name = "user")
 * public class User extends BaseDO {
 *     private String username;
 *     private String email;
 *     // 其他业务字段...
 * }
 * }</pre>
 *
 * @author Jin
 * @since 2025-07-23
 * @see com.baomidou.mybatisplus.annotation.TableField
 * @see com.baomidou.mybatisplus.annotation.TableLogic
 */
@Getter
@Setter
public abstract class BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2694074995776393995L;

    /**
     * 创建用户
     * <p>
     * 记录创建该记录的用户标识，在插入数据时自动填充。
     * 通常存储用户ID或用户名，用于审计追踪。
     * </p>
     */
    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    /**
     * 创建时间
     * <p>
     * 记录该记录的创建时间，在插入数据时自动填充当前时间。
     * 使用LocalDateTime类型，避免时区问题。
     * </p>
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新用户
     * <p>
     * 记录最后更新该记录的用户标识，在更新数据时自动填充。
     * 通常存储用户ID或用户名，用于审计追踪。
     * </p>
     */
    @TableField(fill = FieldFill.UPDATE)
    private String updateUser;

    /**
     * 更新时间
     * <p>
     * 记录该记录的最后更新时间，在更新数据时自动填充当前时间。
     * 使用LocalDateTime类型，避免时区问题。
     * </p>
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
