package com.lambda.cloud.core.base;

/**
 * 枚举基础接口
 * <p>
 * 该接口为所有业务枚举提供统一的规范，要求每个枚举都必须提供一个唯一的编码。
 * 通过统一的接口设计，便于枚举的序列化、反序列化以及数据库存储。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>定义枚举编码的获取规范</li>
 *   <li>支持泛型，允许不同类型的编码</li>
 *   <li>便于枚举与数据库字段的映射</li>
 *   <li>统一枚举的序列化行为</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * public enum UserStatus implements BaseEnum<Integer> {
 *     ACTIVE(1, "激活"),
 *     INACTIVE(0, "未激活"),
 *     LOCKED(-1, "锁定");
 *
 *     private final Integer code;
 *     private final String description;
 *
 *     UserStatus(Integer code, String description) {
 *         this.code = code;
 *         this.description = description;
 *     }
 *
 *     @Override
 *     public Integer getCode() {
 *         return this.code;
 *     }
 * }
 * }</pre>
 *
 * @author Jin
 * @param <I> 编码类型，通常为Integer、String等
 * @see java.lang.Enum
 */
public interface BaseEnum<I> {
    /**
     * 获取枚举编码
     * <p>
     * 返回该枚举项的唯一标识编码，用于数据库存储、API传输等场景。
     * 编码应该具有唯一性和稳定性，一旦确定不应随意更改。
     * </p>
     *
     * <h3>设计原则：</h3>
     * <ul>
     *   <li>编码必须唯一，不能重复</li>
     *   <li>编码应该有意义，便于理解</li>
     *   <li>编码一旦确定，不应轻易修改</li>
     *   <li>建议使用简单类型（如Integer、String）</li>
     * </ul>
     *
     * @return 枚举的唯一编码
     */
    I getCode();
}
