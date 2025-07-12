package com.lambda.security.provider;

import com.lambda.cloud.core.utils.ClassTypeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lambda.cloud.core.Constants.GSON;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThirdPartLoginResult {
    private String thirdType;
    private Object body;

    public <T> T getBody(Class<T> clazz) {
        if (String.class.equals(clazz)) {
            return clazz.cast(body);
        }
        if (ClassTypeUtils.isPrimitiveOrWrapper(clazz)) {
            Object result = ClassTypeUtils.convertPrimitiveOrWrapper(clazz, body.toString());
            return clazz.cast(result);
        }
        return GSON.fromJson(body.toString(), clazz);
    }
}
