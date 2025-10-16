package com.lambda.cloud.core.shared;

import com.lambda.cloud.core.convert.BaseConverter;
import com.lambda.cloud.core.convert.ConverterResolver;
import java.util.List;

/**
 * VO 基类
 *
 * @param <E> 实体类型
 * @author Jin
 */
public abstract class BaseVO<E> {

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
        BaseConverter<E, V> converter = ConverterResolver.getConverter(voClass);
        return converter.convertTo(entity);
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
        BaseConverter<E, V> converter = ConverterResolver.getConverter(voClass);
        return converter.convertToList(entityList);
    }
}
