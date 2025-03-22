package com.lamuda.cloud.swagger.converter;

import cn.hutool.core.lang.ParameterizedTypeImpl;
import com.fasterxml.jackson.databind.JavaType;
import com.lamuda.cloud.swagger.model.Page;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.util.ClassUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * @author w
 */
public class PageConverter implements ModelConverter {

    public static final Type[] TYPES = new Type[0];

    @Override
    public Schema<?> resolve(AnnotatedType annotatedType, ModelConverterContext modelConverterContext, Iterator<ModelConverter> chain) {
        JavaType javaType = Json.mapper().constructType(annotatedType.getType());
        if (javaType != null) {
            Class<?> cls = javaType.getRawClass();
            if (ClassUtils.isAssignable(org.springframework.data.domain.Page.class, cls)) {
                Type[] parameters = javaType.getBindings().getTypeParameters().toArray(TYPES);
                ParameterizedType type = new ParameterizedTypeImpl(parameters, null, Page.class);
                annotatedType = new AnnotatedType(type).resolveAsRef(true);
            }
        }
        return (chain.hasNext()) ? chain.next().resolve(annotatedType, modelConverterContext, chain) : null;
    }
}
