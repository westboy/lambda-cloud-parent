package com.lambda.cloud.netty.protocol.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 基础单元的抽象类
 * <p>
 * 提供了以下核心功能：
 * <ul>
 *   <li>数据读写：支持 ByteBuf 的读写操作</li>
 *   <li>数据转换：支持多种数据格式的转换</li>
 *   <li>数据验证：提供数据有效性验证</li>
 *   <li>字节序处理：支持大小端转换</li>
 *   <li>字符编码：支持多种字符编码</li>
 * </ul>
 * </p>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
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

    /**
     * 数据格式正则表达式
     */
    private String regex;

    /**
     * 大小端标志
     */
    private boolean isLittleEnd;

    /**
     * 字符编码
     */
    private Charset charset = StandardCharsets.UTF_8;

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
        if (byteBuf == null || byteBuf.readableBytes() < this.length) {
            throw new IllegalArgumentException("无效的 ByteBuf 或数据长度不足");
        }
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
            if (byteBuf == null) {
                throw new IllegalArgumentException("ByteBuf 不能为空");
            }
            String writeData = this.data;
            if (isLittleEnd) {
                writeData = UnitKit.highAndLowInversion(writeData);
            }
            byteBuf.writeBytes(Hex.decodeHex(writeData.toCharArray()));
        } catch (DecoderException e) {
            log.error("编码十六进制异常", e);
            throw new RuntimeException("数据编码失败", e);
        }
    }

    /**
     * 获取十六进制数据（去除填充）
     */
    public String getHexData() {
        return this.getEmptyData();
    }

    /**
     * 获取完整的十六进制数据（包含填充）
     */
    public String getFullHexData() {
        return this.data;
    }

    /**
     * 获取原始的十六进制数据（考虑字节序）
     */
    public String getRawHexData() {
        if (isLittleEnd) {
            return UnitKit.highAndLowInversion(this.data);
        }
        return this.data;
    }

    /**
     * 设置数据长度并重新填充数据
     */
    public void setLength(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }
        this.length = length;
        this.fill(this.data);
    }

    /**
     * 设置数据内容并进行长度验证
     */
    public void setData(String data) {
        if (data == null) {
            throw new IllegalArgumentException("数据不能为空");
        }
        int maxLength = this.length * 2;
        if (data.length() > maxLength) {
            throw new IndexOutOfBoundsException(
                String.format("定义长度为：%d；实际长度为：%d", this.length, data.length() / 2)
            );
        }
        fill(data);
    }

    /**
     * 设置填充方向
     */
    @SuppressWarnings("unchecked")
    public <U extends BaseUnit> U setPad(Padding pad) {
        Objects.requireNonNull(pad, "填充方向不能为空");
        if (this.pad != pad) {
            String emptyData = getEmptyData();
            this.pad = pad;
            fill(emptyData);
        }
        return (U) this;
    }

    /**
     * 设置字节序
     */
    @SuppressWarnings("unchecked")
    public <U extends BaseUnit> U setLittleEnd(boolean isLittleEnd) {
        this.isLittleEnd = isLittleEnd;
        return (U) this;
    }

    /**
     * 设置字符编码
     */
    @SuppressWarnings("unchecked")
    public <U extends BaseUnit> U setCharset(Charset charset) {
        this.charset = Objects.requireNonNull(charset, "字符编码不能为空");
        return (U) this;
    }

    /**
     * 转换为字符串
     */
    public String toStringValue() {
        try {
            byte[] bytes = Hex.decodeHex(getEmptyData().toCharArray());
            return new String(bytes, charset);
        } catch (DecoderException e) {
            log.error("解码十六进制异常", e);
            throw new RuntimeException("数据解码失败", e);
        }
    }

    /**
     * 转换为长整型
     */
    public long toLong() {
        try {
            return Long.parseLong(getEmptyData(), 16);
        } catch (NumberFormatException e) {
            log.error("解析长整型异常", e);
            throw new RuntimeException("数据转换失败", e);
        }
    }

    /**
     * 转换为整型
     */
    public int toInt() {
        try {
            return Integer.parseInt(getEmptyData(), 16);
        } catch (NumberFormatException e) {
            log.error("解析整型异常", e);
            throw new RuntimeException("数据转换失败", e);
        }
    }

    /**
     * 转换为字节数组
     */
    public byte[] toBytes() {
        try {
            return Hex.decodeHex(getEmptyData().toCharArray());
        } catch (DecoderException e) {
            log.error("解码十六进制异常", e);
            throw new RuntimeException("数据转换失败", e);
        }
    }

    /**
     * 验证数据是否有效
     */
    public boolean isValid() {
        if (data == null || data.isEmpty()) {
            return false;
        }
        return data.matches("[0-9A-Fa-f]+");
    }

    private void fill(String data) {
        this.data = this.pad.fill(data, this.length);
    }

    private String getEmptyData() {
        return empty(this.data);
    }

    private String empty(String data) {
        return this.pad.empty(data, this.regex);
    }

    @Override
    public String toString() {
        return String.format(
            "BaseUnit{length=%d, data='%s', littleEnd=%b, pad=%s}",
            length, data, isLittleEnd, pad
        );
    }
}
