package com.lambda.cloud.logger.advices;

import com.lambda.cloud.core.exception.NotSupportedException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * 抽象切面基类，为具体的切面实现提供通用的方法执行支持。
 * <p>
 * 该抽象类封装了 AOP 切面的通用逻辑，包括：
 * <ul>
 *     <li>方法签名解析</li>
 *     <li>目标方法获取</li>
 *     <li>切入点执行模板</li>
 * </ul>
 * <p>
 * 子类需要实现 {@link #execute(ProceedingJoinPoint)} 方法来定义具体的切面逻辑。
 *
 * @param <T> 注解类型，必须是 {@link Annotation} 的子类
 * @author jpjoo
 * @since 1.0.0
 */
abstract class AbstractAdvice<T extends Annotation> {

    /**
     * 执行切入点操作的抽象方法。
     * <p>
     * 子类必须实现此方法来定义具体的切面逻辑，如日志记录、性能监控等。
     * 该方法会在目标方法执行前后被调用。
     *
     * @param pjp 环绕通知的连接点，包含目标方法的执行上下文
     * @return 目标方法的执行结果
     * @throws Throwable 如果目标方法执行过程中发生任何异常
     */
    protected abstract Object execute(ProceedingJoinPoint pjp) throws Throwable;

    /**
     * 从连接点获取要执行的目标方法。
     * <p>
     * 该方法通过反射机制从连接点中提取目标方法的 {@link Method} 对象，
     * 用于后续的注解解析、参数获取等操作。
     *
     * @param joinPoint AOP 连接点，包含目标方法的签名信息
     * @return 目标方法的 {@link Method} 对象
     * @throws NoSuchMethodException 如果无法找到对应的方法
     * @throws NotSupportedException 如果连接点不是方法类型
     */
    protected Method getMethodToExecute(JoinPoint joinPoint) throws NoSuchMethodException {
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature methodSignature)) {
            throw new NotSupportedException("This annotation is only valid on a method.");
        }
        Object target = joinPoint.getTarget();
        return target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }
}
