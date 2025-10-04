package com.lambda.cloud.core.shared;

import com.lambda.cloud.core.convert.BaseConverter;
import com.lambda.cloud.core.resolver.ConverterResolver;
import java.util.List;

/**
 * VO 基类
 *
 * @param <E> 实体类型
 * @author Jin
 */
public abstract class BaseVO<E> {

    /**
     * 静态方法：自动推断VO类型并转换单个实体
     * <p>
     * 使用示例：
     * <pre>
     * UserVO userVO = UserVO.fromEntity(userEntity);
     * </pre>
     * <p>
     * 注意：此方法通过调用栈自动推断VO类型，适用于直接从VO子类调用的场景。
     * 如果在其他类中调用，请使用 {@link #fromEntity(Class, Object)} 方法。
     *
     * @param entity 实体对象
     * @param <V>    VO类型参数
     * @param <E>    实体类型参数
     * @return 转换后的VO对象，如果entity为null则返回null
     * @throws IllegalStateException 如果无法推断VO类型
     */
    @SuppressWarnings("unchecked")
    public static <V extends BaseVO<E>, E> V fromEntity(E entity) {
        if (entity == null) {
            return null;
        }
        
        // 使用StackWalker获取调用者类
        Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass();
        
        // 检查调用者是否是BaseVO的子类
        if (BaseVO.class.isAssignableFrom(callerClass) && !BaseVO.class.equals(callerClass)) {
            return fromEntity((Class<V>) callerClass, entity);
        }
        
        throw new IllegalStateException(
            "无法自动推断VO类型。请使用 fromEntity(Class<V> voClass, E entity) 方法并显式传递VO类型。"
        );
    }

    /**
     * 静态方法：自动推断VO类型并转换实体列表
     * <p>
     * 使用示例：
     * <pre>
     * List&lt;UserVO&gt; userVOList = UserVO.fromEntityList(userEntityList);
     * </pre>
     * <p>
     * 注意：此方法通过调用栈自动推断VO类型，适用于直接从VO子类调用的场景。
     * 如果在其他类中调用，请使用 {@link #fromEntityList(Class, List)} 方法。
     *
     * @param entityList 实体列表
     * @param <V>        VO类型参数
     * @param <E>        实体类型参数
     * @return 转换后的VO列表，如果entityList为null或空则返回空列表
     * @throws IllegalStateException 如果无法推断VO类型
     */
    @SuppressWarnings("unchecked")
    public static <V extends BaseVO<E>, E> List<V> fromEntityList(List<E> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return List.of();
        }
        
        // 使用StackWalker获取调用者类
        Class<?> callerClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .getCallerClass();
        
        // 检查调用者是否是BaseVO的子类
        if (BaseVO.class.isAssignableFrom(callerClass) && !BaseVO.class.equals(callerClass)) {
            return fromEntityList((Class<V>) callerClass, entityList);
        }
        
        throw new IllegalStateException(
            "无法自动推断VO类型。请使用 fromEntityList(Class<V> voClass, List<E> entityList) 方法并显式传递VO类型。"
        );
    }

    /**
     * 静态方法：通过VO类型转换单个实体
     * <p>
     * 使用示例：
     * <pre>
     * UserVO userVO = BaseVO.fromEntity(UserVO.class, userEntity);
     * </pre>
     *
     * @param voClass VO类型
     * @param entity  实体对象
     * @param <V>     VO类型参数
     * @param <E>     实体类型参数
     * @return 转换后的VO对象，如果entity为null则返回null
     */
    public static <V extends BaseVO<E>, E> V fromEntity(Class<V> voClass, E entity) {
        if (entity == null) {
            return null;
        }
        BaseConverter<V, E> converter = ConverterResolver.getConverter(voClass);
        return converter.convertFrom(entity);
    }

    /**
     * 静态方法：通过VO类型转换实体列表
     * <p>
     * 使用示例：
     * <pre>
     * List&lt;UserVO&gt; userVOList = BaseVO.fromEntityList(UserVO.class, userEntityList);
     * </pre>
     *
     * @param voClass    VO类型
     * @param entityList 实体列表
     * @param <V>        VO类型参数
     * @param <E>        实体类型参数
     * @return 转换后的VO列表，如果entityList为null或空则返回空列表
     */
    public static <V extends BaseVO<E>, E> List<V> fromEntityList(Class<V> voClass, List<E> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return List.of();
        }
        BaseConverter<V, E> converter = ConverterResolver.getConverter(voClass);
        return converter.convertFromList(entityList);
    }

}
