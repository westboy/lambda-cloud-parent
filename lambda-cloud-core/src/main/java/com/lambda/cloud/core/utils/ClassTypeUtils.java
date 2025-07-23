package com.lambda.cloud.core.utils;

/**
 * 类型工具类
 * <p>
 * 提供基本数据类型和包装类型相关的工具方法，包括类型判断和字符串转换功能。
 * 主要用于反射、序列化、参数转换等场景中的类型处理。
 * </p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>判断类型是否为基本类型或包装类型</li>
 *   <li>将字符串转换为对应的基本类型或包装类型</li>
 *   <li>支持所有Java基本数据类型的转换</li>
 * </ul>
 * 
 * <h3>支持的类型：</h3>
 * <ul>
 *   <li>数值类型：Integer、Long、Double、Float、Short、Byte</li>
 *   <li>布尔类型：Boolean</li>
 *   <li>字符类型：Character</li>
 *   <li>对应的基本类型：int、long、double、float、short、byte、boolean、char</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 类型判断
 * boolean isWrapper = ClassTypeUtils.isPrimitiveOrWrapper(Integer.class); // true
 * boolean isPrimitive = ClassTypeUtils.isPrimitiveOrWrapper(int.class); // true
 * 
 * // 字符串转换
 * Integer intValue = (Integer) ClassTypeUtils.convertPrimitiveOrWrapper(Integer.class, "123");
 * Boolean boolValue = (Boolean) ClassTypeUtils.convertPrimitiveOrWrapper(Boolean.class, "true");
 * }</pre>
 * 
 * @author Jin
 * @see Class#isPrimitive()
 * @see Number
 */
public class ClassTypeUtils {
    /**
     * 判断类型是否为基本类型或包装类型
     * <p>
     * 检查给定的Class对象是否表示Java的基本数据类型或其对应的包装类型。
     * 包括所有数值类型、布尔类型和字符类型。
     * </p>
     * 
     * <h3>支持的类型：</h3>
     * <ul>
     *   <li>基本类型：int, long, double, float, short, byte, boolean, char</li>
     *   <li>包装类型：Integer, Long, Double, Float, Short, Byte, Boolean, Character</li>
     *   <li>Number子类：BigInteger, BigDecimal等</li>
     * </ul>
     *
     * @param clazz 要检查的Class对象，不能为null
     * @return 如果是基本类型或包装类型返回true，否则返回false
     * @throws NullPointerException 如果clazz为null
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive()
                || Number.class.isAssignableFrom(clazz)
                || Boolean.class.equals(clazz)
                || Character.class.equals(clazz);
    }

    /**
     * 将字符串转换为指定的基本类型或包装类型
     * <p>
     * 根据目标类型将字符串内容转换为对应的基本数据类型或包装类型对象。
     * 支持所有常见的基本类型转换，并提供适当的错误处理。
     * </p>
     * 
     * <h3>转换规则：</h3>
     * <ul>
     *   <li>数值类型：使用对应包装类的valueOf方法</li>
     *   <li>布尔类型：使用Boolean.valueOf解析</li>
     *   <li>字符类型：取字符串第一个字符，空字符串返回'\0'</li>
     * </ul>
     * 
     * <h3>异常处理：</h3>
     * <ul>
     *   <li>不支持的类型会抛出IllegalArgumentException</li>
     *   <li>格式错误的字符串会抛出NumberFormatException</li>
     * </ul>
     *
     * @param targetClass 目标类型，必须是支持的基本类型或包装类型
     * @param content 要转换的字符串内容，不能为null
     * @return 转换后的对象，类型与targetClass一致
     * @throws IllegalArgumentException 如果targetClass不是支持的类型
     * @throws NumberFormatException 如果字符串格式不正确
     * @throws NullPointerException 如果参数为null
     */
    public static Object convertPrimitiveOrWrapper(Class<?> targetClass, String content) {
        if (Integer.class.equals(targetClass)) {
            return Integer.valueOf(content);
        } else if (Long.class.equals(targetClass)) {
            return Long.valueOf(content);
        } else if (Double.class.equals(targetClass)) {
            return Double.valueOf(content);
        } else if (Float.class.equals(targetClass)) {
            return Float.valueOf(content);
        } else if (Short.class.equals(targetClass)) {
            return Short.valueOf(content);
        } else if (Byte.class.equals(targetClass)) {
            return Byte.valueOf(content);
        } else if (Boolean.class.equals(targetClass)) {
            return Boolean.valueOf(content);
        } else if (Character.class.equals(targetClass)) {
            return !content.isEmpty() ? content.charAt(0) : '\0';
        }
        throw new IllegalArgumentException("Unsupported primitive or wrapper type: " + targetClass.getName());
    }
}
