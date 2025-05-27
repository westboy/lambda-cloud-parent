package com.lambda.cloud.logger.advices;

import com.lambda.cloud.core.exception.NotSupportedException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * AbstractAdvice
 *
 * @param <T>
 * @author jpjoo
 */
abstract class AbstractAdvice<T extends Annotation> {

    /**
     * 执行切入点操作
     *
     * @param pjp pjp
     * @return object
     * @throws Throwable error stack
     */
    @SuppressWarnings("squid:S112")
    protected abstract Object execute(final ProceedingJoinPoint pjp) throws Throwable;

    Method getMethodToExecute(final JoinPoint jp) throws NoSuchMethodException {
        final Signature sig = jp.getSignature();
        if (!(sig instanceof MethodSignature sing)) {
            throw new NotSupportedException("This annotation is only valid on a method.");
        }
        final Object target = jp.getTarget();

        String name = sing.getName();
        Class<?>[] parameters = sing.getParameterTypes();

        return target.getClass().getMethod(name, parameters);
    }

    /**
     * @param annotation              annotation
     * @param expectedAnnotationClass expectedAnnotationClass
     * @param name                    name
     * @return Object
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    protected Object populate(
            final T annotation, final Class<? extends Annotation> expectedAnnotationClass, String name)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Method namespaceMethod = expectedAnnotationClass.getDeclaredMethod(name, (Class<?>[]) null);
        return namespaceMethod.invoke(annotation, (Object[]) null);
    }
}
