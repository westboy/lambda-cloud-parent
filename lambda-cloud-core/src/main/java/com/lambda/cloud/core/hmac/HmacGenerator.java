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
 * HMAC签名生成器
 * <p>
 * 该工具类用于生成基于HMAC-SHA1算法的API签名，主要用于API接口的安全认证。
 * 提供了构造基础字符串和生成认证信息的功能。
 *
 * <p>签名生成流程：
 * <ol>
 *     <li>构造基础字符串：将请求参数、appid、时间戳、请求体等按规则拼接</li>
 *     <li>使用HMAC-SHA1算法和密钥对基础字符串进行签名</li>
 *     <li>将签名结果进行Base64编码</li>
 *     <li>构造最终的认证头信息</li>
 * </ol>
 *
 * <p>该类使用工具类模式，所有方法均为静态方法，不允许实例化。
 *
 * @author Jin
 * @since 1.0.0
 */
@Slf4j
public final class HmacGenerator {

    /**
     * 用于清理请求体中空白字符和反斜杠的正则表达式
     */
    private static final String REGEX = "\\s+|\\\\";

    /**
     * HMAC认证头前缀
     */
    public static final String HMAC = "HmacSHA ";

    /**
     * 私有构造方法，防止实例化
     */
    private HmacGenerator() {}

    /**
     * 构造基础签名字符串
     * <p>
     * 根据API请求的各项参数构造用于签名的基础字符串。构造规则如下：
     * <ol>
     *     <li>将查询参数按键名排序，格式为：key=value&</li>
     *     <li>添加appid参数：appid=xxx&</li>
     *     <li>添加时间戳参数：timestamp=xxx</li>
     *     <li>如果有请求体，清理空白字符后添加：&body=xxx</li>
     *     <li>对整个字符串进行URL解码</li>
     * </ol>
     *
     * @param appid 应用ID，不能为null
     * @param timestamp 时间戳，Unix时间戳（毫秒）
     * @param queries 查询参数映射，键为参数名，值为参数值数组，可以为null
     * @param body 请求体内容，可以为null
     * @return 构造好的基础签名字符串
     * @throws UnsupportedEncodingException 当URL解码失败时抛出
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

    /**
     * 构造HMAC认证信息
     * <p>
     * 使用HMAC-SHA1算法对基础字符串进行签名，并构造完整的认证头信息。
     * 认证头格式为：HmacSHA appid:timestamp:base64EncodedSignature
     *
     * <p>签名过程：
     * <ol>
     *     <li>使用UTF-8编码将密钥和基础字符串转换为字节数组</li>
     *     <li>使用HMAC-SHA1算法计算签名</li>
     *     <li>将签名结果进行Base64编码</li>
     *     <li>按格式拼接最终的认证头</li>
     * </ol>
     *
     * @param appid 应用ID，不能为null
     * @param secret 应用密钥，不能为null
     * @param timestamp 时间戳，Unix时间戳（毫秒）
     * @param baseString 基础签名字符串，通过baseString方法生成
     * @return 完整的HMAC认证头字符串
     * @throws UnsupportedEncodingException 当字符编码不支持时抛出
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
