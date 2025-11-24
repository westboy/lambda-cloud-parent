package com.lambda.cloud.netty.protocol.converter.impl;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import org.junit.jupiter.api.Test;

class CP56Time2aConverterTest {

    @Test
    void parse() throws ProtocolException {
        String s = "BEB23402D60B19";
        CP56Time2aConverter converter = new CP56Time2aConverter();
        Object parse = converter.parse(
                HexUtil.decodeHex(s),
                new ProtocolFieldMetadata(
                        new FieldAccessor() {
                            @Override
                            public void setValue(Object target, Object value) {
                            }

                            @Override
                            public Object getValue(Object target) {
                                return null;
                            }

                            @Override
                            public String getFieldName() {
                                return "";
                            }

                            @Override
                            public Class<?> getFieldType() {
                                return null;
                            }

                            @Override
                            public void setFieldName(String fieldName) {
                            }

                            @Override
                            public void setFieldType(Class<?> fieldType) {
                            }
                        },
                        null,
                        null,
                        null));

        System.out.println(parse);
    }
}
