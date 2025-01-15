package com.jingfang.security.web.hmac.wrapper;


import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * HmacRequestWrapper
 *
 * @author jpjoo
 */
@Getter
public final class HmacRequestWrapper extends HttpServletRequestWrapper {

    private final String body;

    public HmacRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ServletInputStream input = request.getInputStream();
        StreamUtils.copy(input, output);
        this.body = output.toString(StandardCharsets.UTF_8);
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                body.getBytes((StandardCharsets.UTF_8)));
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
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

}