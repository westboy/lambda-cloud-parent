package com.jingfang.cloud.logger.advices;

import com.jingfang.cloud.logger.annotation.OperationLog;
import com.jingfang.cloud.logger.service.OperationService;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.util.StopWatch;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * OperationLoggerAdvice
 *
 * @author jpjoo
 */
@Aspect
public class OperationLoggerAdvice extends AbstractAdvice<OperationLog> {

    private OperationService operationService;

    @SuppressWarnings("EmptyMethod")
    @Pointcut("@annotation(com.jingfang.cloud.logger.annotation.OperationLog)")
    public void newLogger() {
        //do nothing..
    }

    @Around("newLogger()")
    protected Object obtain(final ProceedingJoinPoint pjp) throws Throwable {
        return execute(pjp);
    }


    @Override
    protected Object execute(ProceedingJoinPoint pjp) throws Throwable {
        StopWatch clock = new StopWatch();
        clock.start();
        Object[] args = pjp.getArgs();
        final Method method = getMethodToExecute(pjp);
        final String methodName = getDeclaredMethodName(method);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        try {
            Object result = pjp.proceed();
            //todo
            return result;
        } finally {
            clock.stop();
            try {
                operationService.save(null);
            } finally {
                MDC.clear();
            }
        }
    }


    private String getDeclaredMethodName(final Method method) {
        return String.format("%s.%s", method.getDeclaringClass().getName(), method.getName());
    }


    private String getOrDefault(String target, String defaultValue) {
        return StringUtils.isNotBlank(target) ? target : defaultValue;
    }
}
