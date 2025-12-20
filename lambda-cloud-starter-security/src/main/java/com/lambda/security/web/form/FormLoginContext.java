package com.lambda.security.web.form;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@SuppressFBWarnings("EI_EXPOSE_REP")
public record FormLoginContext(
        HttpServletRequest request,
        HttpServletResponse response,
        Map<String, Object> requestBody,
        String username,
        String password,
        String device,
        String loginType) {}
