package com.lambda.security.web.hmac.wrapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.Getter;
import org.springframework.util.StreamUtils;

/**
 * HmacRequestWrapper
 *
 * @author jpjoo
 */
@Getter
public final class HmacRequestWrapper extends HttpServletRequestWrapper {

    private final String body;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    private final Map<String, String> hmacHeaders = new HashMap<>();

    public HmacRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ServletInputStream input = request.getInputStream();
        StreamUtils.copy(input, output);
        this.body = output.toString(StandardCharsets.UTF_8);
    }

    public void addHeader(String name, String value) {
        hmacHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        String headerValue = hmacHeaders.get(name);
        if (headerValue != null) {
            return headerValue;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> headerNames = new HashSet<>(hmacHeaders.keySet());
        Enumeration<String> originalHeaders = super.getHeaderNames();
        while (originalHeaders.hasMoreElements()) {
            headerNames.add(originalHeaders.nextElement());
        }
        return Collections.enumeration(headerNames);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> headers = new ArrayList<>();
        if (hmacHeaders.containsKey(name)) {
            headers.add(hmacHeaders.get(name));
        }
        Enumeration<String> originalHeaders = super.getHeaders(name);
        while (originalHeaders.hasMoreElements()) {
            headers.add(originalHeaders.nextElement());
        }
        return Collections.enumeration(headers);
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(body.getBytes((StandardCharsets.UTF_8)));
        return new ServletInputStream() {
            @Override
            public int read() {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
    }
}
