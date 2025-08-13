package com.lambda.cloud.core.base;

/**
 * 数据传输对象基类（Data Transfer Object）
 * <p>
 * DTO用于在不同层之间传输数据，避免直接暴露数据库实体。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>提供实体转换的抽象方法</li>
 *   <li>支持分页查询功能</li>
 *   <li>定义统一的数据传输规范</li>
 *   <li>实现数据层与业务层的解耦</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * public class UserDTO extends BaseDTO<UserVO, User> {
 *     private String username;
 *     private String email;
 *
 *     @Override
 *     public User convertToEntity() {
 *         // 转换为实体对象
 *         return User.builder()
 *             .username(this.username)
 *             .email(this.email)
 *             .build();
 *     }
 *
 *     @Override
 *     public UserVO convertFor(User user) {
 *         // 转换为视图对象
 *         return UserVO.builder()
 *             .username(user.getUsername())
 *             .email(user.getEmail())
 *             .build();
 *     }
 * }
 * }</pre>
 *
 * @param <D> 目标转换类型（通常是VO或其他DTO）
 * @param <E> 源实体类型（通常是数据库实体）
 * @author Jin
 * @since 2025-07-23
 */
public abstract class BaseDTO<D, E> {

    /**
     * 将当前DTO转换为实体对象
     * <p>
     * 该方法用于将数据传输对象转换为数据库实体对象，
     * 通常在保存或更新操作前调用。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>接收前端请求数据后转换为实体</li>
     *   <li>数据验证通过后准备持久化</li>
     *   <li>业务逻辑处理前的数据转换</li>
     * </ul>
     *
     * @return 转换后的实体对象
     */
    public abstract E convertToEntity();

    /**
     * 将实体对象转换为目标类型
     * <p>
     * 该方法用于将数据库实体对象转换为其他类型的对象，
     * 如VO（View Object）或其他DTO，通常在查询操作后调用。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>查询结果转换为前端展示对象</li>
     *   <li>实体对象转换为API响应对象</li>
     *   <li>数据脱敏或字段过滤</li>
     * </ul>
     *
     * @param e 源实体对象
     * @return 转换后的目标对象
     */
    public D convertFor(E e) {
        throw new UnsupportedOperationException("Unsupported operation");
    }
}
