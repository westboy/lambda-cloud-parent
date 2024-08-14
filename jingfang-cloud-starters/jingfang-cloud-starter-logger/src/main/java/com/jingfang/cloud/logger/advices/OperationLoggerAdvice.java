package com.jingfang.cloud.logger.advices;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.jingfang.cloud.core.principal.LoginUser;
import com.jingfang.cloud.core.utils.OperatorUtils;
import com.jingfang.cloud.logger.annotation.OperationLog;
import com.jingfang.cloud.logger.context.LogContext;
import com.jingfang.cloud.logger.model.OperationBody;
import com.jingfang.cloud.logger.model.OperationDetail;
import com.jingfang.cloud.logger.service.OperationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

import static com.jingfang.cloud.core.exception.model.ErrorModel.GSON;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

/**
 * OperationLoggerAdvice
 *
 * @author jpjoo
 */
@Aspect
public class OperationLoggerAdvice extends AbstractAdvice<OperationLog> {

    private final OperationService operationService;

    @Autowired
    public OperationLoggerAdvice(OperationService operationService) {
        this.operationService = operationService;
    }


    @Around("@annotation(com.jingfang.cloud.logger.annotation.OperationLog)")
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
        HttpServletRequest httpRequest = getHttpRequest();
        if (httpRequest == null) {
            return pjp.proceed();
        }
        final LoginUser loginUser = OperatorUtils.getOperator();
        final HttpMethod httpMethod = getDeclaredOperation(httpRequest.getMethod());
        Operation apiOperation = method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class);
        String description = "";
        if (apiOperation != null) {
            description = apiOperation.description();
        }
        OperationLog operationLogger = method.getAnnotation(OperationLog.class);
        String operationId = getOrDefault(operationLogger.value(), methodName);

        OperationBody operationBody = new OperationBody();
        operationBody.setIpaddress(JakartaServletUtil.getClientIP(httpRequest));
        operationBody.setMethod(methodName);
        String operationType = StringUtils.isBlank(operationLogger.type()) ? httpMethod.name() : operationLogger.type();
        operationBody.setOperation(operationType);
        operationBody.setModule(operationLogger.module());
        operationBody.setOperatorId(loginUser.getUsername());

        OperationDetail detail = getOperationDetail(operationId, httpRequest, parameterAnnotations, args);
        try {
            Object result = pjp.proceed();
            detail.setResult(result);
            operationBody.setDetail(getOrDefault(LogContext.getDetail(), GSON.toJson(detail)));
            operationBody.setDescription(getOrDefault(LogContext.getDescription(), description));
            return result;
        } catch (Exception e) {
            detail.setResult(getStackTrace(e));
            operationBody.setDetail(GSON.toJson(detail));
            operationBody.setDescription(description + " - 操作失败");
            throw e;
        } finally {
            clock.stop();
            operationBody.setTime(new Date());
            operationBody.setDuration(clock.getTotalTimeMillis());
            try {
                operationService.save(operationBody);
            } finally {
                MDC.clear();
            }
        }
    }

    private OperationDetail getOperationDetail(String operationId, HttpServletRequest httpRequest, Annotation[][] parameterAnnotations, Object[] args) {
        OperationDetail detail = new OperationDetail();
        detail.setOperationId(operationId);
        detail.setUri(httpRequest.getRequestURI());
        Object body = getDeclaredRequestBody(parameterAnnotations, args);
        if (body != null) {
            detail.setBody(body);
        }
        Map<String, String[]> parameters = httpRequest.getParameterMap();
        if (MapUtils.isNotEmpty(parameters)) {
            detail.setParameters(parameters);
        }
        return detail;
    }


    private Object getDeclaredRequestBody(Annotation[][] parameterAnnotations,
                                          Object[] args) {
        int i = 0;
        for (Annotation[] annotations : parameterAnnotations) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof RequestBody) {
                    return args[i];
                }
            }
            i++;
        }
        return null;
    }


    private HttpMethod getDeclaredOperation(final String method) {
        try {
            HttpMethod operation = HttpMethod.valueOf(method);
            if (operation == HttpMethod.PATCH) {
                operation = HttpMethod.PUT;
            }
            return operation;
        } catch (Exception e) {
            return HttpMethod.GET;
        }
    }

    private HttpServletRequest getHttpRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getRequest();
    }


    private String getDeclaredMethodName(final Method method) {
        return String.format("%s.%s", method.getDeclaringClass().getName(), method.getName());
    }


    private String getOrDefault(String target, String defaultValue) {
        return StringUtils.isNotBlank(target) ? target : defaultValue;
    }
}
