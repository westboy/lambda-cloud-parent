package com.lambda.cloud.mybatis.utils;

import java.util.UUID;

/**
 * @author jfy
 */
public final class UUIDGenerator {

    private UUIDGenerator() {
    }

    public static String getUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int LENGTH = 8;

    /**
     * 生成8位的uuid
     *
     * @return
     */
    public static String getShortUuid() {
        StringBuilder builder = new StringBuilder();
        String uuid = getUuid();
        for (int i = 0; i < LENGTH; i++) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            builder.append(CHARS[x % 0x3E]);
        }
        return builder.toString();
    }
}
