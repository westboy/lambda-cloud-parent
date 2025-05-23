package com.lambda.cloud.mybatis.mapping;

import java.util.Map;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

/**
 * @author Ji
 */
public class LambdaBoundSql extends BoundSql {

    public LambdaBoundSql(Configuration configuration, org.apache.ibatis.mapping.BoundSql source, String sql) {
        super(configuration, sql, source.getParameterMappings(), source.getParameterObject());
        MetaObject object = SystemMetaObject.forObject(source);
        @SuppressWarnings("unchecked")
        Map<String, Object> additionalParameters = (Map<String, Object>) object.getValue("additionalParameters");
        for (Map.Entry<String, Object> item : additionalParameters.entrySet()) {
            setAdditionalParameter(item.getKey(), item.getValue());
        }
    }
}
