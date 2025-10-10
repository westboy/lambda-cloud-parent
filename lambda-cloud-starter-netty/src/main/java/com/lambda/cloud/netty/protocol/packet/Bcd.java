package com.lambda.cloud.netty.protocol.packet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * BCD码
 */
@Getter
@Setter
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class Bcd extends BaseUnit {

    private int precision;

    private int offset;

    public Bcd(int length) {
        this(length, 0, 0);
    }

    public Bcd(int length, int precision) {
        this(length, precision, 0);
    }

    public Bcd(int length, int precision, int offset) {
        super(length);
        this.precision = precision;
        this.offset = offset;
    }

    public void setData(int data) {
        this.setData(Integer.toString(data));
    }

    public void setData(double data) {
        this.setData(Double.toString(data));
    }

    @Override
    public void setData(String data) {
        BigDecimal bigDecimal = new BigDecimal(data).multiply(new BigDecimal(10).pow(this.precision));
        BigInteger bigInteger = bigDecimal.toBigInteger().add(new BigInteger(Integer.toString(this.offset)));
        super.setData(bigInteger.toString());
    }

    /**
     * 从 BCD 字节数组设置数据
     * 例如：0x01,0x59 -> 159
     */
    public void setData(byte[] bcdBytes) {
        int intValue = bcdBytesToInt(bcdBytes);
        this.setData(intValue);
    }

    /**
     * BCD byte[] 转 int
     */
    private int bcdBytesToInt(byte[] bcdBytes) {
        int result = 0;
        for (byte b : bcdBytes) {
            int high = (b >> 4) & 0x0F;
            int low = b & 0x0F;
            result = result * 100 + high * 10 + low;
        }
        return result;
    }

    public int getIntData() {
        int intData = Integer.parseInt(super.getData()) - this.offset;
        return intData / (int) (Math.pow(10, this.precision));
    }

    public Double getDoubleData() {
        double data = Integer.parseInt(super.getData()) - this.offset * 1.0;
        return data / Math.pow(10, this.precision);
    }

    public BigDecimal getBigDecimalData() {
        BigInteger integerData = new BigInteger(this.getHexData()).subtract(BigInteger.valueOf(offset));
        return new BigDecimal(integerData).divide(BigDecimal.valueOf(10).pow(this.precision), RoundingMode.DOWN);
    }

    @Override
    public String getData() {
        return this.getPrintData();
    }

    @Override
    public String getPrintData() {
        return UnitKit.toNumString(this.offset, this.precision, new BigInteger(this.getHexData()));
    }

    @Override
    public String getHexData() {
        String hexData = super.getHexData();
        if (hexData.isEmpty()) {
            return "0";
        } else {
            return hexData;
        }
    }
}
