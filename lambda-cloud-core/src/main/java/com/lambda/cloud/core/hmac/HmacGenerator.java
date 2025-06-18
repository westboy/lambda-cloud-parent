package com.lambda.cloud.core.hmac;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @author Jin
 */
@Slf4j
public final class HmacGenerator {
    private static final String REGEX = "\\s+|\\\\";
    public static final String HMAC = "HmacSHA ";

    private HmacGenerator() {}

    /***
     * 构造基础数据
     *
     * @param appid String
     * @param timestamp long
     * @param queries Map<String, String[]>
     * @param body String
     * @return java.lang.String
     */
    @SneakyThrows
    public static String baseString(
            String appid, long timestamp, Map<String, String[]> queries, @Nullable String body) {
        StringBuilder baseString = new StringBuilder();
        if (MapUtils.isNotEmpty(queries)) {
            queries.keySet().stream().sorted().forEach(key -> {
                String[] values = queries.get(key);
                if (ArrayUtils.isNotEmpty(values)) {
                    baseString.append(key).append("=");
                    baseString.append(values.length == 1 ? values[0] : Arrays.toString(values));
                    baseString.append("&");
                }
            });
        }
        baseString.append("appid=").append(appid).append("&");
        baseString.append("timestamp=").append(timestamp);
        if (StringUtils.isNotBlank(body)) {
            body = body.replaceAll(REGEX, EMPTY);
            baseString.append("&body=").append(body);
        }

        String decodedString = URLDecoder.decode(baseString.toString(), StandardCharsets.UTF_8.displayName());
        log.trace("baseString: {}", decodedString);
        return decodedString;
    }

    /***
     * 构造认证信息
     * @param appid appid
     * @param secret  secret
     * @param timestamp 时间戳
     * @param baseString 原始数据
     * @return java.lang.String
     */
    public static String authorization(String appid, String secret, long timestamp, String baseString)
            throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        builder.append(HMAC);
        builder.append(appid);
        builder.append(":");
        builder.append(timestamp);
        builder.append(":");
        final byte[] key = secret.getBytes(StandardCharsets.UTF_8);
        final byte[] digest = baseString.getBytes(StandardCharsets.UTF_8);
        byte[] bytes =
                HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_1, key).doFinal(digest);
        builder.append(Base64.encodeBase64String(bytes));
        return builder.toString();
    }
}
