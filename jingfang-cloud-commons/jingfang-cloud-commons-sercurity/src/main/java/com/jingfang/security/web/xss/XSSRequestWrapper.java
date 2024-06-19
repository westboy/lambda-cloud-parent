package com.jingfang.security.web.xss;

import com.jingfang.cloud.web.DefaultServletInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * XSSRequestWrapper
 *
 * @author Jin
 */
@Slf4j
@SuppressWarnings({"AlibabaClassNamingShouldBeCamel", "PMD"})
public class XSSRequestWrapper extends HttpServletRequestWrapper {

    public static final String[] STRINGS = new String[0];

    public XSSRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (ArrayUtils.isNotEmpty(values)) {
            return Arrays.stream(values).map(this::encode).toArray(String[]::new);
        }
        return values;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return encode(value);
    }


    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> result = new ArrayList<>();
        Enumeration<String> headers = super.getHeaders(name);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            String[] values = header.split(",");
            for (String value : values) {
                result.add(encode(value));
            }
        }
        return Collections.enumeration(result);
    }


    @Override
    public BufferedReader getReader() throws IOException {
        String body = IOUtils.toString(super.getReader());
        return new BufferedReader(new StringReader(encode(body)));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        String body = IOUtils.toString(super.getInputStream(), StandardCharsets.UTF_8);
        return new DefaultServletInputStream(encode(body));
    }

    public String encode(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        try {
            value = ESAPI.encoder().canonicalize(value).replace("\0", StringUtils.EMPTY);
            return Jsoup.clean(value, Safelist.none());
        } catch (IntrusionException e) {
            log.info("If you are sure to trust the request, add the following:\nspring:\n  security:\n    xss-protected:\n      trusted: {}", this.getRequestURI());
            return StringUtils.EMPTY;
        }
    }


}
