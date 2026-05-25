package com.lambda.cloud.t645.message;

import java.util.Locale;

public final class T645CodecSupport {

    private T645CodecSupport() {}

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

    public static String buildDiKey(int controlCode, String di) {
        if (di == null) {
            di = "";
        }
        return String.format("%02X:%s", controlCode & 0xFF, di.toUpperCase(Locale.ROOT));
    }
}
