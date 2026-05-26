package com.lambda.cloud.t645.message;

import java.util.Locale;

/**
 * DL/T 645-2007 协议编解码辅助工具类。
 *
 * <p>提供 T645 协议帧中常用的编解码操作：</p>
 * <ul>
 *   <li>数据域 +0x33/-0x33 偏移运算（协议规定的加解密方式）</li>
 *   <li>地址域字节序翻转（协议规定低字节在前传输）</li>
 *   <li>SUM8 校验和计算</li>
 *   <li>DI 查找键构建</li>
 * </ul>
 */
public final class T645CodecSupport {

    private T645CodecSupport() {}

    /**
     * 数据域减去 0x33 偏移（解码时使用）。
     *
     * <p>DL/T 645-2007 协议规定数据域每个字节均加上 0x33 后传输，
     * 解码时需将每个字节减去 0x33 还原原始数据。</p>
     *
     * @param data 已加偏移的数据字节数组
     * @return 减去 0x33 后的原始数据，若输入为 null 则返回空数组
     */
    public static byte[] subOffset33(byte[] data) {
        if (data == null) {
            return new byte[0];
        }
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) ((data[i] - 0x33) & 0xFF);
        }
        return result;
    }

    /**
     * 数据域加上 0x33 偏移（编码时使用）。
     *
     * <p>发送报文时需对数据域每个字节加 0x33，与 {@link #subOffset33(byte[])} 互为逆运算。</p>
     *
     * @param data 原始数据字节数组
     * @return 加上 0x33 后的数据，若输入为 null 则返回空数组
     */
    public static byte[] addOffset33(byte[] data) {
        if (data == null) {
            return new byte[0];
        }
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) ((data[i] + 0x33) & 0xFF);
        }
        return result;
    }

    /**
     * 翻转地址域字节序。
     *
     * <p>DL/T 645-2007 协议规定地址域采用低字节在前（Little-Endian）传输，
     * 而显示/逻辑使用时为高字节在前（Big-Endian），因此需要进行字节序翻转。</p>
     *
     * <p>例如传输序 {@code A1A2A3A4A5A6} 翻转后为 {@code A6A5A4A3A2A1}。</p>
     *
     * @param address 12 个十六进制字符组成的地址字符串（6 字节 BCD 编码）
     * @return 翻转后的地址字符串（大写）
     * @throws IllegalArgumentException 地址长度不为 12 时抛出
     */
    public static String reverseAddress(String address) {
        if (address == null || address.length() != 12) {
            throw new IllegalArgumentException("地址域长度必须为12个十六进制字符（6字节）");
        }
        StringBuilder reversed = new StringBuilder(12);
        for (int i = 10; i >= 0; i -= 2) {
            reversed.append(address, i, i + 2);
        }
        return reversed.toString().toUpperCase(Locale.ROOT);
    }

    /**
     * 计算 SUM8 校验和。
     *
     * <p>将指定数据所有字节累加后取低 8 位，作为帧校验码 CS。</p>
     *
     * @param data 参与校验和计算的数据字节数组
     * @return SUM8 校验和（0x00~0xFF），若输入为 null 则返回 0
     */
    public static int calculateSum8(byte[] data) {
        if (data == null) {
            return 0;
        }
        int sum = 0;
        for (byte b : data) {
            sum = (sum + (b & 0xFF)) & 0xFF;
        }
        return sum;
    }

    /**
     * 构建控制码与数据标识的组合键，用于载荷注册表查找。
     *
     * <p>格式：{@code 控制码(2位大写HEX):DI(大写HEX)}，例如 {@code 11:00010000}。</p>
     *
     * @param controlCode 控制码
     * @param di 数据标识，为 null 或空时使用 {@code NONE}
     * @return 组合键字符串
     */
    public static String buildDiKey(int controlCode, String di) {
        if (di == null || di.isEmpty()) {
            di = "NONE";
        }
        return String.format("%02X:%s", controlCode & 0xFF, di.toUpperCase(Locale.ROOT));
    }
}
