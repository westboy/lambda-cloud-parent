package com.lambda.cloud.netty.protocol.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

/**
 * 基础单元的抽象类
 *
 */
@Data
@Slf4j
public abstract class BaseUnit implements Unit {
    /**
     * 数据的进制
     */
    public static final int DEFAULT_RADIX = 16;

    /**
     * 所占长度
     */
    private int length;

    /**
     * 大端十六进制完整数据
     */
    private String data;

    /**
     * 补全数据的方向
     */
    private Padding pad;

    private String regex;

    /**
     * 大小端
     */
    private boolean isLittleEnd;

    protected BaseUnit(int length) {
        this(length, true);
    }

    protected BaseUnit(int length, boolean isNumber) {
        this.length = length;
        data = StringUtils.leftPad("", length * 2, "0");
        this.pad = Padding.LEFT;

        this.regex = isNumber ? "0" : "00";
    }

    @Override
    public void read(ByteBuf byteBuf) {
        String dump = ByteBufUtil.hexDump(byteBuf, byteBuf.readerIndex(), this.length);
        if (isLittleEnd) {
            dump = UnitKit.highAndLowInversion(dump);
        }
        this.data = dump;
        byteBuf.skipBytes(this.length);
    }

    @Override
    public void write(ByteBuf byteBuf) {
        try {
            String writeData = this.data;
            if (isLittleEnd) {
                writeData = UnitKit.highAndLowInversion(writeData);
            }
            byteBuf.writeBytes(Hex.decodeHex(writeData.toCharArray()));
        } catch (DecoderException e) {
            log.error("编码十六进制异常", e);
        }
    }

    public String getHexData() {
        return this.getEmptyData();
    }

    public String getFullHexData() {
        return this.data;
    }

    public String getRawHexData() {
        if (isLittleEnd) {
            return UnitKit.highAndLowInversion(this.data);
        }
        return this.data;
    }

    public void setLength(int length) {
        this.length = length;
        this.fill(this.data);
    }

    public void setData(String data) {
        int maxLength = this.length * 2;
        if (data.length() > maxLength) {
            throw new IndexOutOfBoundsException(String.format("定义长度为：%d；实际长度为：%d；", this.length, data.length() / 2));
        }
        fill(data);
    }

    @SuppressWarnings("unchecked")
    public <U extends BaseUnit> U setPad(Padding pad) {
        if (this.pad != pad) {
            String emptyData = getEmptyData();
            this.pad = pad;
            fill(emptyData);
        }
        return (U) this;
    }

    @SuppressWarnings("unchecked")
    public <U extends BaseUnit> U setLittleEnd(boolean isLittleEnd) {
        this.isLittleEnd = isLittleEnd;
        return (U) this;
    }

    private void fill(String data) {
        this.data = this.pad.fill(data, this.length);
    }

    public String getEmptyData() {
        return empty(this.data);
    }

    private String empty(String data) {
        return this.pad.empty(data, this.regex);
    }
}
