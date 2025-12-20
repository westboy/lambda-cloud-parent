package com.lambda.security.web.form;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public record FormLoginContext(
        HttpServletRequest request,
        HttpServletResponse response,
        Map<String, Object> requestBody,
        String username,
        String password,
        String device,
        String loginType) {}
