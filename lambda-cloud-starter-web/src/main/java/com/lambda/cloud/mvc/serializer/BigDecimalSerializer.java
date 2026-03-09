package com.lambda.cloud.mvc.serializer;

import java.math.BigDecimal;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * BigDecimal系列化成String，防止保留2位小数时，尾数丢失0的为问题。
 * <br/>
 * 1 默认序列化方式：20.00 -> 20
 * <br/>
 * 2 使用@JsonSerialize(using = BigDecimalSerializer.class)， 20.00 -> 20.00
 * @author Jin
 */
public class BigDecimalSerializer extends StdSerializer<BigDecimal> {

    public BigDecimalSerializer() {
        this(null);
    }

    public BigDecimalSerializer(Class<BigDecimal> c) {
        super(c);
    }

    @Override
    public void serialize(BigDecimal value, tools.jackson.core.JsonGenerator gen, SerializationContext provider)
            throws JacksonException {
        gen.writeString(value.toPlainString());
    }
}
