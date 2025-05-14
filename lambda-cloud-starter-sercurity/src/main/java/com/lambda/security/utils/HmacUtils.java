package com.lambda.security.utils;

import com.lambda.cloud.core.utils.HmacGenerator;
import com.lambda.security.web.hmac.model.HmacAuthorization;
import com.lambda.security.web.hmac.wrapper.HmacRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lambda.cloud.mvc.WebHttpUtils.AUTHORIZATION;

/**
 * HmacFactory
 *
 * @author jpjoo
 */
@Slf4j
public final class HmacUtils {

    private HmacUtils() {
    }

    private static final String PATTERN = "^(\\w+) (\\S+):(\\S+):(\\S+)$";

    public static HmacAuthorization getHmacAuthorization(HttpServletRequest request) {
        final Matcher matcher = matcher(request);

        if (matcher.matches()) {
            HmacAuthorization authorization = new HmacAuthorization();
            authorization.setAppid(matcher.group(2));
            authorization.setTimestamp(matcher.group(3));
            authorization.setDigest(matcher.group(4));
            return authorization;
        }
        return null;
    }

    public static Matcher matcher(HttpServletRequest request) {
        final String header = request.getHeader(AUTHORIZATION);
        final Pattern pattern = Pattern.compile(PATTERN);
        return pattern.matcher(header);
    }


    public static String getHmacSaltValue(final HmacRequestWrapper request, String appid, String timestamp) {
        Map<String, String[]> queries = request.getParameterMap();
        String body = null;
        if (hasBody(request)) {
            body = request.getBody();
        }
        return HmacGenerator.baseString(appid, Long.parseLong(timestamp), queries, body);
    }

    private static boolean hasBody(HmacRequestWrapper request) {
        String body = request.getBody();
        RequestMethod method = RequestMethod.valueOf(request.getMethod());
        return StringUtils.isNotBlank(body) && (RequestMethod.POST.equals(method) || RequestMethod.PUT.equals(method));
    }
}
