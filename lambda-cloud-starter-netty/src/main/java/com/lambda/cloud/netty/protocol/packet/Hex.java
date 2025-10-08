package com.lambda.cloud.netty.protocol.packet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 取16进制值
 */
@Getter
@Setter
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class Hex extends BaseUnit {

    private int precision;

    private int offset;

    public Hex(int length) {
        this(length, 0, 0);
    }

    public Hex(int length, int precision) {
        this(length, precision, 0);
    }

    public Hex(int length, int precision, int offset) {
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

        if (bigInteger.compareTo(BigInteger.ZERO) >= 0) {
            this.setSuperData(bigInteger.toString());
        } else {
            log.error("设置的值经过偏移量计算后小于零异常, 源数据: {}; 偏移量: {}; 结果: {};", data, this.offset, bigInteger);
        }
    }

    public void setData(String data, int radix) {
        this.setData(new BigInteger(data, radix).toString());
    }

    public void setSuperData(String data) {
        BigInteger bigInteger = new BigInteger(data);
        super.setData(bigInteger.toString(16));
    }

    public int getIntData() {
        int intData = Integer.parseInt(super.getData(), 16) - this.offset;
        return intData / (int) (Math.pow(10, this.precision));
    }

    public double getDoubleData() {
        double data = Integer.parseInt(super.getData(), 16) - this.offset * 1.0;
        return data / Math.pow(10, this.precision);
    }

    public BigDecimal getBigDecimalData() {
        BigInteger integerData = new BigInteger(this.getHexData(), 16).subtract(BigInteger.valueOf(offset));
        return new BigDecimal(integerData).divide(BigDecimal.valueOf(10).pow(this.precision), RoundingMode.DOWN);
    }

    @Override
    public String getData() {
        return this.getPrintData();
    }

    @Override
    public String getPrintData() {
        return UnitKit.toNumString(this.offset, this.precision, new BigInteger(this.getHexData(), 16));
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
