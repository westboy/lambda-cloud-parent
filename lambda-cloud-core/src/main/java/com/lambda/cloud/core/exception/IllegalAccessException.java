package com.lambda.cloud.core.exception;

import java.io.Serial;

/**
 * 非法访问异常
 * <p>
 * 当应用程序尝试通过反射创建实例（数组除外）、设置或获取字段、调用方法时，
 * 但当前执行的方法没有访问指定类、字段、方法或构造函数定义的权限时，
 * 会抛出此异常。
 * </p>
 * 
 * <p>该异常继承自RuntimeException，是一个运行时异常，通常在以下情况下抛出：</p>
 * <ul>
 *   <li>反射访问私有成员时权限不足</li>
 *   <li>跨包访问受保护成员时权限不足</li>
 *   <li>访问不可访问的类或接口</li>
 *   <li>模块系统中的访问控制限制</li>
 * </ul>
 * 
 * <h3>常见使用场景：</h3>
 * <ul>
 *   <li>框架中的反射操作权限检查</li>
 *   <li>安全相关的访问控制</li>
 *   <li>动态代理中的权限验证</li>
 *   <li>序列化/反序列化过程中的访问控制</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 当尝试访问私有字段时
 * if (!field.isAccessible()) {
 *     throw new IllegalAccessException("无法访问私有字段: " + field.getName());
 * }
 * 
 * // 当权限检查失败时
 * if (!hasPermission(user, resource)) {
 *     throw new IllegalAccessException("用户没有访问该资源的权限");
 * }
 * }</pre>
 *
 * @author unascribed
 * @see Class#newInstance()
 * @see java.lang.reflect.Field#set(Object, Object)
 * @see java.lang.reflect.Field#setBoolean(Object, boolean)
 * @see java.lang.reflect.Field#setByte(Object, byte)
 * @see java.lang.reflect.Field#setShort(Object, short)
 * @see java.lang.reflect.Field#setChar(Object, char)
 * @see java.lang.reflect.Field#setInt(Object, int)
 * @see java.lang.reflect.Field#setLong(Object, long)
 * @see java.lang.reflect.Field#setFloat(Object, float)
 * @see java.lang.reflect.Field#setDouble(Object, double)
 * @see java.lang.reflect.Field#get(Object)
 * @see java.lang.reflect.Field#getBoolean(Object)
 * @see java.lang.reflect.Field#getByte(Object)
 * @see java.lang.reflect.Field#getShort(Object)
 * @see java.lang.reflect.Field#getChar(Object)
 * @see java.lang.reflect.Field#getInt(Object)
 * @see java.lang.reflect.Field#getLong(Object)
 * @see java.lang.reflect.Field#getFloat(Object)
 * @see java.lang.reflect.Field#getDouble(Object)
 * @see java.lang.reflect.Method#invoke(Object, Object[])
 * @see java.lang.reflect.Constructor#newInstance(Object[])
 * @since JDK1.0
 */
public class IllegalAccessException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 6616958222490762034L;

    /**
     * 构造一个带有详细消息的IllegalAccessException
     * <p>
     * 创建一个新的非法访问异常实例，包含指定的错误详细信息。
     * 详细消息应该清楚地描述访问失败的原因和上下文。
     * </p>
     *
     * @param s 详细错误消息，描述异常的具体原因
     */
    public IllegalAccessException(String s) {
        super(s);
    }
}
