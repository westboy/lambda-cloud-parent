package com.lamuda.cloud.core.utils;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.cglib.core.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 使用Cglib高效的复制对象，同名属性能自动处理类型转换问题， 注意：
 * <ul>
 *      <li>1 对象使用了Lombok注解时，不要有{@link Accessors}注解，否则无法复制。</li>
 *      <li>2 重要问题说三遍: 浅copy、浅copy、浅copy, 注意引用问题, 如有后续需要会增强此工具类转换为深copy。</li>
 *      <li>3 是否使用转换器，对性能会有影响，请选择适当的方法使用！</li>
 * </ul>
 * 如果使用java 16 17版本，请在项目启动类中配置jvm参数：--add-opens java.base/java.lang=ALL-UNNAMED
 *
 * @author Jin
 */
@Slf4j
public class FastCopyUtils {

    /**
     * 用于将对象属性同名不同类型之间转换，比如 String age -> Int age。
     * 如果都是转换对象直接，类型一致，不用转换器性则能最好。
     */
    private static final ConverterSupport CONVERTER_SUPPORT = new ConverterSupport();

    protected static final Map<CopyCacheKey, BeanCopier> CACHE_MAP = new ConcurrentHashMap<>();

    private FastCopyUtils() {
    }


    public static <T> T copyNoConverter(Object form, T to) {
        return copy(form, to.getClass(), to, null);
    }

    /**
     * 类型一致情况下，强烈推荐使用，可传入对象构造函数。
     * <code>
     * <pre>
     *         FastCopyUtils.copyNoConverter(obj, Bar::new);
     *     </pre>
     * </code>
     *
     * @param form 原对象
     * @param toFc 目标对象实例化方法
     * @param <T>  目标泛型
     * @return 目标对象
     */
    public static <T> T copyNoConverter(Object form, Supplier<T> toFc) {
        T to = toFc.get();
        return copy(form, to.getClass(), to, null);
    }


    /**
     * 使用转换器情况下，对象复制性能最好，可传入对象构造函数。
     * <code>
     * <pre>
     *         FastCopyUtils.copy(obj, Bar::new);
     *     </pre>
     * </code>
     *
     * @param form 源对象
     * @param toFc 目标对象实例化方法
     * @param <T>  目标泛型
     * @return 目标对象
     */
    public static <T> T copy(Object form, Supplier<T> toFc) {
        T to = toFc.get();
        return copy(form, to.getClass(), to, CONVERTER_SUPPORT);
    }

    /**
     * 使用转换器情况下，对象复制性能最好。
     *
     * @param form 源对象
     * @param to   目标对象
     * @param <T>  目标泛型
     * @return 目标对象
     */
    public static <T> T copy(Object form, T to) {
        return copy(form, to.getClass(), to, CONVERTER_SUPPORT);
    }

    /**
     * 使用转换器情况下，此方法需要反射实例化，性能略低，推荐{@link #copy(Object, Object)}传入实例化对象的方式。
     *
     * @param form  源对象
     * @param toClz 目标类型
     * @param <T>   目标泛型
     * @return 目标对象
     */
    public static <T> T copy(Object form, Class<T> toClz) {
        T toSource = null;
        try {
            toSource = toClz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("创建对象发生异常，请确认存在默认空的构造方法: ", e);
            return null;
        }

        return copy(form, toClz, toSource, CONVERTER_SUPPORT);
    }

    /**
     * 列表复制，强烈推荐，示例：
     * <code>
     * <pre>
     *         List<Foo> objects = new ArrayList<>();
     *         FastCopyUtils.listCopy(fooList, Bar::new);
     *     </pre>
     * </code>
     *
     * @param formList 源数组
     * @param toFc     目标对象实例化方法
     * @param <F>      源对象泛型
     * @param <T>      目标对象泛型
     * @return 目标数组
     */
    public static <F, T> List<T> listCopy(List<F> formList, Supplier<T> toFc) {
        return formList.parallelStream().map(v -> copy(v, toFc.get())).collect(Collectors.toList());
    }

    /**
     * 列表复制，性能较低。
     *
     * @param formList 源数组
     * @param toClz    目标对象
     * @param <F>      源对象泛型
     * @param <T>      目标对象泛型
     * @return 目标数组
     */
    public static <F, T> List<T> listCopy(List<F> formList, Class<T> toClz) {
        return formList.parallelStream().map(v -> copy(v, toClz)).collect(Collectors.toList());
    }

    public static <T> T copy(Object form, Class<?> toClz, T to, Converter converter) {
        if (form == null) {
            return to;
        }
        final Class<?> formClass = form.getClass();
        BeanCopier beanCopier = CACHE_MAP.computeIfAbsent(buildCacheKey(formClass, toClz, converter),
                key -> BeanCopier.create(formClass, toClz, converter != null));
        beanCopier.copy(form, to, converter);
        return to;
    }

    private static CopyCacheKey buildCacheKey(Class<?> formClass, Class<?> toClass, Converter converter) {
        return new CopyCacheKey(formClass, toClass, converter != null);
    }

    static class CopyCacheKey {
        Class<?> formClass;
        Class<?> toClass;
        boolean userConverter;

        public CopyCacheKey(Class<?> formClass, Class<?> toClass, boolean userConverter) {
            this.formClass = formClass;
            this.toClass = toClass;
            this.userConverter = userConverter;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CopyCacheKey)) {
                return false;
            }
            if (userConverter != ((CopyCacheKey) other).userConverter) {
                return false;
            }
            CopyCacheKey otherKey = (CopyCacheKey) other;
            return (this.formClass.equals(otherKey.formClass)) &&
                    this.toClass.equals(otherKey.toClass) &&
                    (userConverter == otherKey.userConverter);
        }

        @Override
        public int hashCode() {
            return (this.formClass.hashCode() * 29 + this.toClass.hashCode() + (userConverter ? 1 : 0));
        }

        @Override
        public String toString() {
            return ("CopyCacheKey [formClass = " + this.formClass +
                    ", toClass = " + this.toClass + ", userConverter = " + userConverter + "]");
        }
    }

    public static class ConverterSupport implements Converter {

        DefaultConversionService convert = new DefaultConversionService();

        @Override
        @SuppressWarnings("unchecked")
        public Object convert(Object o, Class aClass, Object o1) {
            if (o != null && convert.canConvert(o.getClass(), aClass)) {
                return convert.convert(o, aClass);
            }
            return null;
        }
    }

}
