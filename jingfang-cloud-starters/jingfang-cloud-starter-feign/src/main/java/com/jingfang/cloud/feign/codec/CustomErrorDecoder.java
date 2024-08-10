package com.jingfang.cloud.feign.codec;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jingfang.cloud.core.exception.feign.*;
import com.jingfang.cloud.core.exception.model.ErrorModel;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

/**
 * CustomErrorDecoder
 *
 * @author westboy
 */
public class CustomErrorDecoder implements ErrorDecoder {

    private final Gson gson = new Gson();

    @Override
    public Exception decode(String methodKey, Response response) {
        Exception exception;
        int status = response.status();
        try {
            ErrorModel model = gson.fromJson(response.body().asReader(StandardCharsets.UTF_8), ErrorModel.class);
            exception = switch (status) {
                case 400 -> new FeignArgumentNotValidException(model);
                case 401 -> new FeignUnauthorizedException(model);
                case 403 -> new FeignAccessDeniedException(model);
                case 503 -> new FeignServiceNotAvailableException(model);
                default -> new FeignInternalServerErrorException(model);
            };
        } catch (JsonSyntaxException e) {
            ErrorModel model = new ErrorModel();
            model.setStatus(status);
            model.setPath(response.request().url());
            model.setTimestamp(System.currentTimeMillis());
            model.setMessage(response.body().toString());
            model.setError(MessageFormat.format("error status: {0}", status));
            exception = new FeignInternalServerErrorException(model);
        } catch (IOException ignored) {
            ErrorModel model = new ErrorModel();
            model.setStatus(500);
            model.setPath(response.request().url());
            model.setTimestamp(System.currentTimeMillis());
            model.setMessage("External Server Error");
            model.setError(MessageFormat.format("error status: {0}", status));
            exception = new FeignInternalServerErrorException(model);
        }
        return exception;
    }

}
