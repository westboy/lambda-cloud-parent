package com.lambda.security.provider;

import com.lambda.cloud.core.utils.ClassTypeUtils;
import lombok.Data;

import static com.lambda.cloud.core.Constants.GSON;

@Data
public class ThirdPartLoginResult {
    private String thirdType;
    private String body;

    public ThirdPartLoginResult(String thirdType, Object body) {
        this.thirdType = thirdType;
        this.body = GSON.toJson(body);
    }

    public <T> T getBody(Class<T> clazz) {
        if (String.class.equals(clazz)) {
            return clazz.cast(body);
        }
        if (ClassTypeUtils.isPrimitiveOrWrapper(clazz)) {
            Object result = ClassTypeUtils.convertPrimitiveOrWrapper(clazz, body);
            return clazz.cast(result);
        }
        return GSON.fromJson(body, clazz);
    }
}
