package com.lambda.cloud.netty.protocol.annotation;

import lombok.Getter;

/**
 * 协议数据类型枚举
 * <p>
 * 定义协议解析中支持的数据类型
 * </p>
 *
 */
@Getter
public enum ProtocolDataType {

    /**
     * 十六进制数据
     */
    HEX("十六进制"),

    /**
     * ASCII 字符串
     */
    ASCII("ASCII字符串"),

    /**
     * BCD 编码
     */
    BCD("BCD编码"),

    /**
     * 位数据
     */
    BIT("位数据"),

    /**
     * 无符号整数（1字节）
     */
    UINT8("无符号8位整数"),

    /**
     * 无符号整数（2字节）
     */
    UINT16("无符号16位整数"),

    /**
     * 无符号整数（4字节）
     */
    UINT32("无符号32位整数"),

    /**
     * 无符号整数（8字节）
     */
    UINT64("无符号64位整数"),

    /**
     * 时间戳（CP56Time2a格式，7字节）
     */
    CP56TIME2A("CP56Time2a时间格式"),

    /**
     * 复合类型
     */
    COMPOSITE("COMPOSITE"),

    /**
     * 列表类型（支持List集合）
     */
    LIST("列表类型");

    /**
     * 获取数据类型描述
     */
    private final String description;

    ProtocolDataType(String description) {
        this.description = description;
    }

    /**
     * 获取默认长度（字节数）
     *
     * @return 默认长度，-1表示需要显式指定
     */
    public int getDefaultLength() {
        return switch (this) {
            case UINT8 -> 1;
            case UINT16 -> 2;
            case UINT32 -> 4;
            case UINT64 -> 8;
            case CP56TIME2A -> 7;
            default -> -1; // 需要显式指定长度
        };
    }
}
