package com.lambda.cloud.netty.protocol.packet;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * 根据AscII编码的字段
 *
 */
@Slf4j
public class Ascii extends BaseUnit {
    /**
     * 编码方式
     */
    private final Charset charset;

    public Ascii(int length) {
        this(length, StandardCharsets.US_ASCII);
    }

    public Ascii(int length, Charset charset) {
        super(length, false);
        this.charset = charset;
    }

    public void setElasticData(String data) {
        String hexData = this.getHexData(data);
        super.setLength(hexData.length() / 2);
        super.setData(hexData);
    }

    @Override
    public void setData(String data) {
        super.setData(this.getHexData(data));
    }

    @Override
    public String getData() {
        return toStringValue();
    }

    public String getHexData(String data) {
        byte[] bytes = data.getBytes(this.charset);
        return Hex.encodeHexString(bytes);
    }

    @Override
    public String getPrintData() {
        return this.getData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Ascii ascii = (Ascii) o;
        return Objects.equals(charset, ascii.charset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), charset);
    }
}
