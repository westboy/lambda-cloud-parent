package com.lambda.cloud.mvc.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.math.BigDecimal;

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
    public void serialize(BigDecimal bigDecimal, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(bigDecimal.toPlainString());
    }
}
