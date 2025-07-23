package com.lambda.cloud.core.convert;

/**
 * 双向转换器接口
 * <p>
 * 定义了两种类型之间的双向转换操作。该接口提供了类型A到类型B的正向转换，
 * 以及类型B到类型A的反向转换功能。
 *
 * <p>常用于以下场景：
 * <ul>
 *     <li>DTO与Entity之间的转换</li>
 *     <li>VO与DTO之间的转换</li>
 *     <li>不同数据格式之间的转换</li>
 *     <li>API请求/响应对象的转换</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * public class UserConverter implements Converter<UserEntity, UserDTO> {
 *     @Override
 *     public UserDTO doForward(UserEntity entity) {
 *         // 实现Entity到DTO的转换
 *         return new UserDTO(entity.getId(), entity.getName());
 *     }
 *
 *     @Override
 *     public UserEntity doBackward(UserDTO dto) {
 *         // 实现DTO到Entity的转换
 *         UserEntity entity = new UserEntity();
 *         entity.setId(dto.getId());
 *         entity.setName(dto.getName());
 *         return entity;
 *     }
 * }
 * }</pre>
 *
 * @param <A> 源类型
 * @param <B> 目标类型
 * @author Jin
 * @since 1.0.0
 */
public interface Converter<A, B> {

    /**
     * 正向转换
     * <p>
     * 将类型A的对象转换为类型B的对象。
     *
     * @param a 源对象，类型为A
     * @return 转换后的对象，类型为B
     * @throws IllegalArgumentException 如果输入参数无效
     * @throws UnsupportedOperationException 如果不支持该转换操作
     */
    B doForward(A a);

    /**
     * 反向转换
     * <p>
     * 将类型B的对象转换为类型A的对象。
     *
     * @param b 源对象，类型为B
     * @return 转换后的对象，类型为A
     * @throws IllegalArgumentException 如果输入参数无效
     * @throws UnsupportedOperationException 如果不支持该转换操作
     */
    A doBackward(B b);
}
