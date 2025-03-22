package com.lamuda.cloud.mybatis.support;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.Map;

/**
 * @author Ji
 */
public class BoundSql extends org.apache.ibatis.mapping.BoundSql {

    public BoundSql(Configuration configuration, org.apache.ibatis.mapping.BoundSql source, String sql) {
        super(configuration, sql, source.getParameterMappings(), source.getParameterObject());
        MetaObject object = SystemMetaObject.forObject(source);
        @SuppressWarnings("unchecked")
        Map<String, Object> additionalParameters = (Map<String, Object>) object.getValue("additionalParameters");
        for (Map.Entry<String, Object> item : additionalParameters.entrySet()) {
            setAdditionalParameter(item.getKey(), item.getValue());
        }
    }
}
