package com.jingfang.cloud.core.propertis;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.List;

@Data
public class CorsProperties {

    public static final String ALL_PATH = "/**";

    public static final ImmutableSet<String> ALLOWED_METHOD = ImmutableSet.copyOf(new String[]{
            "DELETE",
            "GET",
            "HEAD",
            "OPTIONS",
            "POST",
            "PUT",
            "TRACE",
            "PATCH",
    });

    public static final ImmutableSet<String> EXPOSED_HEADERS = ImmutableSet.copyOf(new String[]{
            HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
    });
    public static final ImmutableSet<String> ALLOWED_HEADERS = ImmutableSet.copyOf(new String[]{
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_TYPE,
            "X-Content-Type",
            "x-requested-with"
    });

    private boolean enable;

    @Nullable
    private List<String> allowedOrigins;

    private long maxAge = 3600L;
}