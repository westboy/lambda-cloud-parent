package com.lamuda.cloud.feign.hmac;

import com.google.common.collect.Maps;
import com.lamuda.cloud.core.utils.HmacGenerator;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.http.HttpMethod;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

/**
 * HmacClientRequestInterceptor
 *
 * @author westboy
 */
@Slf4j
public class HmacClientRequestInterceptor implements RequestInterceptor {

    private final String appid;
    private final String secret;

    public HmacClientRequestInterceptor(String appid, String secret) {
        this.appid = appid;
        this.secret = secret;
    }


    @SneakyThrows
    @Override
    public void apply(RequestTemplate template) {
        long timestamp = System.currentTimeMillis();
        Map<String, String[]> queries = getQueries(template.queries());
        String body = getRequestBody(template.body(), template.method());
        String baseString = HmacGenerator.baseString(appid, timestamp, queries, body);
        String authorization = HmacGenerator.authorization(appid, secret, timestamp, baseString);
        template.removeHeader(AUTHORIZATION);
        template.header(AUTHORIZATION, authorization);
    }

    @Nullable
    private String getRequestBody(byte[] body, String methodName) {
        HttpMethod method = HttpMethod.valueOf(methodName);
        boolean existing = ArrayUtils.isNotEmpty(body) && (POST.equals(method) || PUT.equals(method));
        if (existing) {
            return new String(body, UTF_8);
        }
        return null;
    }


    private static Map<String, String[]> getQueries(Map<String, Collection<String>> queries) {
        Map<String, String[]> converted = Maps.newLinkedHashMap();
        for (Map.Entry<String, Collection<String>> entry : queries.entrySet()) {
            Collection<String> values = entry.getValue();
            if (CollectionUtils.isNotEmpty(values)) {
                converted.put(entry.getKey(), values.toArray(new String[0]));
            } else {
                converted.put(entry.getKey(), new String[]{EMPTY});
            }
        }
        return converted;
    }
}
