package com.lambda.cloud.mvc.execption;

import cn.dev33.satoken.exception.SaTokenException;
import com.lambda.cloud.core.exception.IllegalAccessException;
import com.lambda.cloud.core.exception.IllegalArgumentException;
import com.lambda.cloud.core.exception.IllegalStateException;
import com.lambda.cloud.core.exception.NotSupportedException;
import com.lambda.cloud.core.exception.feign.AbstractFeignException;
import com.lambda.cloud.core.exception.feign.FeignArgumentNotValidException;
import com.lambda.cloud.core.exception.model.ArgumentError;
import com.lambda.cloud.core.exception.model.ErrorModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * @author Jin
 */
@Slf4j
@ResponseBody
@ControllerAdvice
public class GlobalControllerAdvice {

    private ErrorModel handler400(HttpServletRequest request) {
        ErrorModel model = new ErrorModel();
        model.setPath(request.getRequestURI());
        model.setTimestamp(System.currentTimeMillis());
        model.setStatus(HttpStatus.BAD_REQUEST.value());
        model.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        return model;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ErrorModel handler400V1(Exception exception, HttpServletRequest request) {
        ErrorModel model = handler400(request);
        model.setMessage("请求参数错误, " + exception.getMessage());
        return model;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ErrorModel handler400V2(Exception exception, HttpServletRequest request) {
        ErrorModel model = handler400(request);
        model.setMessage("请求参数错误, " + exception.getMessage());
        return model;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorModel handler400(ConstraintViolationException exception, HttpServletRequest request) {
        ErrorModel model = handler400(request);
        model.setMessage("请求参数错误, " + exception.getMessage());
        return model;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public ErrorModel handler400(BindException exception, HttpServletRequest request) {
        ErrorModel model = handler400(request);
        model.setErrors(obtainArgumentErrors(exception.getBindingResult()));
        return model;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ErrorModel handler400(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        ErrorModel model = handler400(request);
        model.setMessage("文件上传发生异常，" + exception.getMessage());
        return model;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({SaTokenException.class})
    public ErrorModel handler401(Exception exception, HttpServletRequest request) {
        ErrorModel model = new ErrorModel();
        model.setPath(request.getRequestURI());
        model.setTimestamp(System.currentTimeMillis());
        model.setStatus(HttpStatus.UNAUTHORIZED.value());
        if (exception instanceof SaTokenException saTokenException) {
            model.setError(String.valueOf(saTokenException.getCode()));
        } else {
            model.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
        }
        model.setMessage(exception.getMessage());
        return model;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({IllegalAccessException.class})
    public ErrorModel handler403(Exception exception, HttpServletRequest request) {
        ErrorModel model = new ErrorModel();
        model.setPath(request.getRequestURI());
        model.setTimestamp(System.currentTimeMillis());
        model.setStatus(HttpStatus.FORBIDDEN.value());
        model.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
        model.setMessage("无效请求，" + exception.getMessage());
        return model;
    }

    public ErrorModel handler500(HttpServletRequest request) {
        ErrorModel model = new ErrorModel();
        model.setPath(request.getRequestURI());
        model.setTimestamp(System.currentTimeMillis());
        model.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        return model;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorModel handler500(Exception exception, HttpServletRequest request) {
        log.error(exception.getMessage(), exception);
        ErrorModel model = handler500(request);
        model.setMessage("500 - 服务器异常！");
        return model;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({
        HttpRequestMethodNotSupportedException.class,
        IllegalStateException.class,
        NotSupportedException.class
    })
    public ErrorModel handler500(HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        ErrorModel model = handler500(request);
        model.setMessage(exception.getMessage());
        return model;
    }

    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    @ExceptionHandler(BusinessException.class)
    private ErrorModel handler501(BusinessException exception, HttpServletRequest request) {
        ErrorModel model = new ErrorModel();
        model.setPath(request.getRequestURI());
        model.setTimestamp(System.currentTimeMillis());
        model.setStatus(HttpStatus.NOT_IMPLEMENTED.value());
        model.setMessage(exception.getMessage());
        model.setError(exception.getCode());
        return model;
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(AbstractFeignException.class)
    public ErrorModel handler503(AbstractFeignException exception) {
        ErrorModel model = new ErrorModel();
        model.setPath(exception.getPath());
        model.setTimestamp(exception.getTimestamp());
        model.setStatus(exception.getStatus());
        model.setError(exception.getError());
        model.setMessage(exception.getMessage());
        if (ClassUtils.isAssignableValue(FeignArgumentNotValidException.class, exception)) {
            model.setErrors(((FeignArgumentNotValidException) exception).getErrors());
        }
        return model;
    }

    private List<ArgumentError> obtainArgumentErrors(@NonNull BindingResult bindingResult) {
        List<ArgumentError> errors = new ArrayList<>();
        List<ObjectError> objectErrors = bindingResult.getAllErrors();
        for (ObjectError error : objectErrors) {
            String field = error.getObjectName();
            String defaultMessage = error.getDefaultMessage();
            if (error instanceof FieldError fieldError) {
                field = fieldError.getField();
            }
            errors.add(new ArgumentError(field, defaultMessage));
        }
        return errors;
    }
}
