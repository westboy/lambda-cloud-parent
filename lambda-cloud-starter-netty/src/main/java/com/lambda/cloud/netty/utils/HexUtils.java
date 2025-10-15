package com.lambda.cloud.netty.utils;

import java.util.Arrays;

/**
 * 十六进制工具类
 * <p>
 * 提供高效的十六进制数据转换方法
 * </p>
 *
 * @author Jin
 */
public final class HexUtils {

    /**
     * 十六进制字符数组
     */
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    /**
     * 十六进制字符到数值的映射表
     */
    private static final int[] HEX_VALUES = new int[256];

    static {
        // 初始化映射表
        Arrays.fill(HEX_VALUES, -1);
        for (int i = 0; i < 10; i++) {
            HEX_VALUES['0' + i] = i;
        }
        for (int i = 0; i < 6; i++) {
            HEX_VALUES['A' + i] = 10 + i;
            HEX_VALUES['a' + i] = 10 + i;
        }
    }

    /**
     * 私有构造函数
     */
    private HexUtils() {
        throw new UnsupportedOperationException("工具类不能实例化");
    }

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 十六进制字符串转字节数组
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     * @throws IllegalArgumentException 无效的十六进制字符串
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }

        // 移除空格
        hex = hex.replaceAll("\\s+", "");

        // 确保长度为偶数
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("十六进制字符串长度必须为偶数: " + hex);
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int high = getHexValue(hex.charAt(i * 2));
            int low = getHexValue(hex.charAt(i * 2 + 1));

            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("无效的十六进制字符: " + hex);
            }

            bytes[i] = (byte) ((high << 4) | low);
        }
        return bytes;
    }

    /**
     * 获取十六进制字符的数值
     *
     * @param c 十六进制字符
     * @return 数值，-1表示无效字符
     */
    private static int getHexValue(char c) {
        return c < HEX_VALUES.length ? HEX_VALUES[c] : -1;
    }

    /**
     * 验证是否为有效的十六进制字符串
     *
     * @param hex 十六进制字符串
     * @return 是否有效
     */
    public static boolean isValidHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            return false;
        }

        hex = hex.replaceAll("\\s+", "");

        if (hex.length() % 2 != 0) {
            return false;
        }

        for (char c : hex.toCharArray()) {
            if (getHexValue(c) == -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * 整数转十六进制字符串（指定长度）
     *
     * @param value  整数值
     * @param length 字符串长度
     * @return 十六进制字符串
     */
    public static String intToHex(int value, int length) {
        String hex = Integer.toHexString(value).toUpperCase();
        if (hex.length() < length) {
            return "0".repeat(length - hex.length()) + hex;
        } else if (hex.length() > length) {
            return hex.substring(hex.length() - length);
        }
        return hex;
    }

    /**
     * 长整数转十六进制字符串（指定长度）
     *
     * @param value  长整数值
     * @param length 字符串长度
     * @return 十六进制字符串
     */
    public static String longToHex(long value, int length) {
        String hex = Long.toHexString(value).toUpperCase();
        if (hex.length() < length) {
            return "0".repeat(length - hex.length()) + hex;
        } else if (hex.length() > length) {
            return hex.substring(hex.length() - length);
        }
        return hex;
    }

    /**
     * 反转字节数组（大小端转换）
     *
     * @param bytes 原始字节数组
     * @return 反转后的字节数组
     */
    public static byte[] reverseBytes(byte[] bytes) {
        if (bytes == null || bytes.length <= 1) {
            return bytes;
        }

        byte[] reversed = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            reversed[i] = bytes[bytes.length - 1 - i];
        }
        return reversed;
    }

    /**
     * 反转十六进制字符串的字节序（每两个字符为一个字节）
     *
     * @param hex 十六进制字符串
     * @return 反转字节序后的十六进制字符串
     */
    public static String reverseHexBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return hex;
        }

        // 移除空格
        hex = hex.replaceAll("\\s+", "");

        // 确保长度为偶数
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("十六进制字符串长度必须为偶数: " + hex);
        }

        StringBuilder result = new StringBuilder(hex.length());

        // 从后往前，每两个字符为一组
        for (int i = hex.length() - 2; i >= 0; i -= 2) {
            result.append(hex.charAt(i)).append(hex.charAt(i + 1));
        }

        return result.toString();
    }

    /**
     * 根据字节序转换字节数组
     *
     * @param bytes         原始字节数组
     * @param littleEndian  是否为小端序
     * @return 转换后的字节数组
     */
    public static byte[] convertEndianness(byte[] bytes, boolean littleEndian) {
        if (!littleEndian) {
            // 大端序，不需要转换
            return bytes;
        }
        // 小端序，需要反转
        return reverseBytes(bytes);
    }

    /**
     * 根据字节序转换十六进制字符串
     *
     * @param hex          十六进制字符串
     * @param littleEndian 是否为小端序
     * @return 转换后的十六进制字符串
     */
    public static String convertHexEndianness(String hex, boolean littleEndian) {
        if (!littleEndian) {
            // 大端序，不需要转换
            return hex;
        }
        // 小端序，需要反转字节序
        return reverseHexBytes(hex);
    }
}
