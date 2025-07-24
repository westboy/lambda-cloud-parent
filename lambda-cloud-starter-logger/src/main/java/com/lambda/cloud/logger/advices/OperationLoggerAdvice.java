package com.lambda.cloud.logger.advices;

import static com.lambda.cloud.core.Constants.GSON;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.OperatorUtils;
import com.lambda.cloud.logger.annotation.OperationLog;
import com.lambda.cloud.logger.context.LogContext;
import com.lambda.cloud.logger.model.OperationLogRecord;
import com.lambda.cloud.logger.model.OperationContext;
import com.lambda.cloud.logger.service.OperationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpMethod;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志切面类，用于拦截标注 {@code @OperationLog} 的方法并自动记录操作日志。
 * <p>
 * 该切面类基于 Spring AOP 实现，主要功能包括：
 * <ul>
 *     <li>拦截标注了 {@code @OperationLog} 注解的方法</li>
 *     <li>自动收集方法执行的上下文信息（参数、返回值、异常等）</li>
 *     <li>记录方法执行时间和性能指标</li>
 *     <li>提取 HTTP 请求相关信息（IP、URI、参数等）</li>
 *     <li>构建完整的操作日志并通过 {@link OperationService} 进行保存</li>
 * </ul>
 * <p>
 * 切面处理流程：
 * <ol>
 *     <li>方法执行前：收集请求信息、用户信息、方法签名等</li>
 *     <li>方法执行中：启动计时器，执行目标方法</li>
 *     <li>方法执行后：记录执行结果、计算耗时、构建日志对象</li>
 *     <li>异常处理：捕获异常信息并记录到日志中</li>
 *     <li>资源清理：清除 MDC 上下文，保存日志</li>
 * </ol>
 * <p>
 * 支持的信息收集：
 * <ul>
 *     <li>HTTP 信息：请求方法、URI、IP 地址、请求参数</li>
 *     <li>用户信息：操作人员 ID、用户名等</li>
 *     <li>方法信息：类名、方法名、参数、返回值</li>
 *     <li>执行信息：开始时间、执行耗时、执行结果</li>
 *     <li>异常信息：异常类型、异常消息、堆栈跟踪</li>
 * </ul>
 *
 * @author jpjoo
 * @since 1.0.0
 * @see OperationLog
 * @see OperationService
 * @see AbstractAdvice
 */
@Slf4j
@Aspect
public class OperationLoggerAdvice extends AbstractAdvice<OperationLog> {

    /**
     * 操作日志服务实例，用于保存操作日志。
     * <p>
     * 该服务负责处理操作日志的持久化或传输，具体实现由注入的服务决定。
     */
    private final OperationService operationService;

    /**
     * 构造方法，注入操作日志服务。
     * <p>
     * 通过依赖注入的方式获取 {@link OperationService} 实例，
     * 确保切面能够正确保存操作日志。
     *
     * @param operationService 操作日志服务实例，不能为 {@code null}
     * @throws IllegalArgumentException 如果 operationService 为 {@code null}
     */
    public OperationLoggerAdvice(OperationService operationService) {
        if (operationService == null) {
            throw new IllegalArgumentException("OperationService must not be null");
        }
        this.operationService = operationService;
    }

    /**
     * 环绕通知，拦截标注了 {@code @OperationLog} 注解的方法。
     * <p>
     * 该方法是 AOP 切面的入口点，负责拦截所有标注了 {@code @OperationLog} 注解的方法，
     * 并委托给 {@link #execute(ProceedingJoinPoint)} 方法进行具体的日志处理。
     *
     * @param joinPoint 环绕通知的连接点，包含目标方法的执行上下文
     * @return 目标方法的执行结果
     * @throws Throwable 如果目标方法执行过程中抛出异常
     */
    @Around("@annotation(com.lambda.cloud.logger.annotation.OperationLog)")
    protected Object obtain(ProceedingJoinPoint joinPoint) throws Throwable {
        return execute(joinPoint);
    }

    /**
     * 执行操作日志记录的核心逻辑。
     * <p>
     * 该方法实现了完整的操作日志记录流程，包括：
     * <ol>
     *     <li>收集方法执行的上下文信息</li>
     *     <li>启动性能计时器</li>
     *     <li>执行目标方法</li>
     *     <li>处理执行结果或异常</li>
     *     <li>构建并保存操作日志</li>
     *     <li>清理资源</li>
     * </ol>
     *
     * @param joinPoint 环绕通知的连接点
     * @return 目标方法的执行结果
     * @throws Throwable 如果目标方法执行过程中抛出异常
     */
    @Override
    protected Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        // 启动性能计时器，记录方法执行时长
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        // 收集方法执行的基本信息
        Object[] methodArgs = joinPoint.getArgs();
        Method targetMethod = getMethodToExecute(joinPoint);
        String methodName = buildMethodName(targetMethod);
        Annotation[][] parameterAnnotations = targetMethod.getParameterAnnotations();
        
        // 获取操作日志注解
        OperationLog operationLogAnnotation = targetMethod.getAnnotation(OperationLog.class);
        if (operationLogAnnotation == null) {
            log.debug("方法 {} 未标注 @OperationLog 注解，跳过日志记录", methodName);
            return joinPoint.proceed();
        }
        
        // 获取 HTTP 请求上下文
        HttpServletRequest httpRequest = getCurrentHttpRequest();
        if (httpRequest == null) {
            // 非 Web 环境，直接执行目标方法
            log.debug("非Web环境，无法获取HTTP请求信息，跳过日志记录");
            return joinPoint.proceed();
        }
        
        // 收集用户和请求信息
        LoginUser currentUser = OperatorUtils.getOperator();
        HttpMethod httpMethod = parseHttpMethod(httpRequest.getMethod());
        String swaggerDescription = getSwaggerDescription(targetMethod);
        String operationId = StringUtils.defaultIfBlank(operationLogAnnotation.value(), methodName);

        // 构建操作日志记录对象
        OperationLogRecord operationLogRecord = buildOperationLogRecord(
            methodName, httpMethod, operationLogAnnotation, currentUser, httpRequest
        );
        
        // 构建操作上下文对象
        OperationContext operationContext = buildOperationContext(
            operationId, httpRequest, parameterAnnotations, methodArgs
        );
        
        try {
            // 执行目标方法
            Object methodResult = joinPoint.proceed();
            
            // 记录成功执行的结果
            operationContext.setResult(methodResult);
            
            // 设置日志详情和描述
            String detailJson = StringUtils.defaultIfBlank(
                LogContext.getDetail(), 
                GSON.toJson(operationContext)
            );
            operationLogRecord.setDetail(detailJson);
            
            String finalDescription = StringUtils.defaultIfBlank(
                LogContext.getDescription(), 
                swaggerDescription
            );
            operationLogRecord.setDescription(finalDescription);
            
            return methodResult;
            
        } catch (Exception exception) {
            // 记录异常执行的结果
            operationContext.setResult(getStackTrace(exception));
            operationLogRecord.setDetail(GSON.toJson(operationContext));
            operationLogRecord.setDescription(swaggerDescription + " - 操作执行失败");
            
            // 重新抛出异常，不影响业务流程
            throw exception;
            
        } finally {
            // 停止计时并设置执行信息
            stopWatch.stop();
            operationLogRecord.setTime(new Date());
            operationLogRecord.setDuration(stopWatch.getTotalTimeMillis());
            
            // 保存操作日志
            saveOperationLogSafely(operationLogRecord);
            
            // 清理 MDC 上下文
            LogContext.clear();
        }
    }

    /**
     * 构建操作日志记录对象
     * <p>根据方法信息、HTTP请求信息、用户信息等构建完整的操作日志记录</p>
     *
     * @param methodName 目标方法名称
     * @param httpMethod HTTP请求方法类型
     * @param operationLogAnnotation 操作日志注解
     * @param currentUser 当前登录用户信息
     * @param httpRequest HTTP请求对象
     * @return 构建完成的操作日志记录对象
     */
    private OperationLogRecord buildOperationLogRecord(String methodName, 
                                                       HttpMethod httpMethod,
                                                       OperationLog operationLogAnnotation, 
                                                       LoginUser currentUser,
                                                       HttpServletRequest httpRequest) {
        OperationLogRecord operationLogRecord = new OperationLogRecord();
        
        // 设置基本信息
        operationLogRecord.setMethod(methodName);
        operationLogRecord.setModule(operationLogAnnotation.module());
        
        // 设置HTTP方法类型，优先使用注解配置，否则使用实际HTTP方法
        String operationType = StringUtils.defaultIfBlank(
            operationLogAnnotation.type(), 
            httpMethod.name()
        );
        operationLogRecord.setHttpMethod(operationType);
        
        // 设置操作人信息
        String operatorId = (currentUser != null) ? currentUser.getUsername() : "unknown";
        operationLogRecord.setOperatorId(operatorId);
        
        // 设置客户端IP地址
        operationLogRecord.setIpAddress(JakartaServletUtil.getClientIP(httpRequest));
        
        return operationLogRecord;
    }
    
    /**
     * 构建操作上下文对象
     * <p>根据操作ID、HTTP请求信息、方法参数等构建操作上下文</p>
     *
     * @param operationId 操作标识ID
     * @param httpRequest HTTP请求对象
     * @param parameterAnnotations 方法参数注解数组
     * @param methodArgs 方法参数值数组
     * @return 构建完成的操作上下文对象
     */
    private OperationContext buildOperationContext(String operationId, 
                                                   HttpServletRequest httpRequest,
                                                   Annotation[][] parameterAnnotations, 
                                                   Object[] methodArgs) {
        OperationContext operationContext = new OperationContext();
        
        // 设置基本信息
        operationContext.setOperationId(operationId);
        operationContext.setUri(httpRequest.getRequestURI());
        
        // 设置请求参数和请求体
        Object requestBody = getDeclaredRequestBody(parameterAnnotations, methodArgs);
        if (requestBody != null) {
            operationContext.setBody(requestBody);
        }
        
        Map<String, String[]> parameters = httpRequest.getParameterMap();
        if (MapUtils.isNotEmpty(parameters)) {
            operationContext.setParameters(parameters);
        }
        
        return operationContext;
    }
    
    /**
     * 安全地保存操作日志
     * <p>在独立的事务中保存操作日志，确保即使保存失败也不影响主业务流程</p>
     *
     * @param operationLogRecord 待保存的操作日志记录对象
     */
    private void saveOperationLogSafely(OperationLogRecord operationLogRecord) {
        try {
            operationService.save(operationLogRecord);
        } catch (Exception exception) {
            // 记录日志保存失败的情况，但不影响业务流程
            log.error("操作日志保存失败: {}", exception.getMessage(), exception);
        }
    }

    /**
     * 获取方法参数中标注了@RequestBody注解的参数值
     * <p>遍历方法的所有参数，查找标注了@RequestBody注解的参数并返回其值</p>
     *
     * @param parameterAnnotations 方法参数注解二维数组，每个参数对应一个注解数组
     * @param args 方法参数值数组
     * @return 标注了@RequestBody的参数值，如果没有找到则返回null
     */
    private Object getDeclaredRequestBody(Annotation[][] parameterAnnotations, Object[] methodArgs) {
        for (int paramIndex = 0; paramIndex < parameterAnnotations.length; paramIndex++) {
            for (Annotation annotation : parameterAnnotations[paramIndex]) {
                if (annotation instanceof RequestBody) {
                    return methodArgs[paramIndex];
                }
            }
        }
        return null;
    }

    /**
     * 解析HTTP请求方法类型，并进行必要的转换处理
     * <p>将字符串形式的HTTP方法转换为HttpMethod枚举，同时处理特殊情况：</p>
     * <ul>
     *     <li>将PATCH方法转换为PUT方法进行统一处理</li>
     *     <li>对于无法识别的方法，默认返回GET方法</li>
     * </ul>
     *
     * @param method HTTP方法字符串（如"GET"、"POST"、"PUT"等）
     * @return 对应的HttpMethod枚举值，无法识别时返回GET
     */
    private HttpMethod parseHttpMethod(String method) {
        try {
            HttpMethod operation = HttpMethod.valueOf(method);
            if (operation == HttpMethod.PATCH) {
                return HttpMethod.PUT;
            }
            return operation;
        } catch (IllegalArgumentException e) {
            return HttpMethod.GET; // 默认 GET
        }
    }

    /**
     * 获取当前线程绑定的HTTP请求对象
     * <p>通过Spring的RequestContextHolder获取当前请求上下文中的HttpServletRequest对象</p>
     * <p>该方法适用于Web环境，在非Web环境或异步线程中可能返回null</p>
     *
     * @return 当前HTTP请求对象，如果不在Web请求上下文中则返回null
     */
    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes != null ? requestAttributes.getRequest() : null;
    }

    /**
     * 构建方法的全限定名称标识
     * <p>将方法对象转换为"类名.方法名"的格式，用于日志记录和方法标识</p>
     * <p>使用简单类名而非全限定类名，以提高日志的可读性</p>
     *
     * @param method 目标方法的Method对象
     * @return 格式为"类名.方法名"的字符串标识
     */
    private String buildMethodName(Method method) {
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }

    /**
     * 获取异常的完整堆栈跟踪信息
     * <p>将异常对象转换为包含完整堆栈信息的字符串，用于日志记录和问题排查</p>
     * <p>该方法会捕获异常的类型、消息以及完整的调用堆栈信息</p>
     *
     * @param throwable 需要获取堆栈信息的异常对象
     * @return 包含完整堆栈跟踪信息的字符串，包括异常类型、消息和调用链
     */
    private String getStackTrace(Throwable throwable) {
        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {
            
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
            
        } catch (Exception exception) {
            // 如果获取堆栈信息失败，返回基本的异常信息
            return throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        }
    }

    /**
     * 获取方法的Swagger API描述信息
     * <p>按优先级顺序从以下注解中提取API描述信息：</p>
     * <ol>
     *     <li>OpenAPI 3.x的@Operation注解的description属性</li>
     *     <li>OpenAPI 3.x的@Operation注解的summary属性</li>
     *     <li>如果都没有配置，则使用方法名作为默认描述</li>
     * </ol>
     *
     * @param method 目标方法的Method对象
     * @return API描述信息，优先返回注解配置的描述，否则返回方法名
     */
    private String getSwaggerDescription(Method method) {
        Operation operation = method.getAnnotation(Operation.class);
        if (operation != null) {
            // 优先使用 description 属性
            if (StringUtils.isNotBlank(operation.description())) {
                return operation.description();
            }
            // 其次使用 summary 属性
            if (StringUtils.isNotBlank(operation.summary())) {
                return operation.summary();
            }
        }
        
        // 默认使用方法名
        return method.getName();
    }
}
